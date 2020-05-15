package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.AliasDao
import com.criptext.mail.db.dao.CustomDomainDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONObject


class CreateSessionWorker(val httpClient: HttpClient,
                          private val db: SignInLocalDB,
                          private val isMultiple: Boolean,
                          signUpDao: SignUpDao,
                          private val name : String,
                          private val username: String,
                          private val domain: String,
                          private val accountDao: AccountDao,
                          private val aliasDao: AliasDao,
                          private val customDomainDao: CustomDomainDao,
                          private val keyValueStorage: KeyValueStorage,
                          private val keyGenerator: SignalKeyGenerator,
                          private val messagingInstance: MessagingInstance,
                          private val ephemeralJwt: String,
                          private val randomId: Int,
                          override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.CreateSessionFromLink> {

    private val apiClient = SignInAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(signUpDao, keyValueStorage, accountDao, aliasDao, customDomainDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.CreateSessionFromLink {
        return SignInResult.CreateSessionFromLink.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.CreateSessionFromLink>): SignInResult.CreateSessionFromLink? {

        val result = Result.of {
                    val lastLoggedUser = AccountUtils.getLastLoggedAccounts(keyValueStorage)
                    if(!isMultiple) {
                        if (lastLoggedUser.isNotEmpty()) {
                            db.deleteDatabase(lastLoggedUser)
                            db.deleteSystemLabels()
                            keyValueStorage.remove(listOf(KeyValueStorage.StringKey.LastLoggedUser))
                        } else {
                            db.deleteDatabase()
                            keyValueStorage.clearAll()
                        }
                    } else {
                        val email = username.plus("@$domain")
                        if (lastLoggedUser.isNotEmpty() && email in lastLoggedUser) {
                            db.deleteDatabase(username, domain)
                            lastLoggedUser.removeAll { it == email }
                            keyValueStorage.putString(KeyValueStorage.StringKey.LastLoggedUser, lastLoggedUser.distinct().joinToString())
                        } else if(lastLoggedUser.isNotEmpty()) {
                            db.deleteDatabase(lastLoggedUser)
                            lastLoggedUser.clear()
                            keyValueStorage.putString(KeyValueStorage.StringKey.LastLoggedUser, lastLoggedUser.distinct().joinToString())
                        }
                    }

                }
                .flatMap { signalRegistrationOperation() }
                .flatMap(storeAccountOperation)
                .flatMap { Result.of {  ActiveAccount.loadFromStorage(keyValueStorage)!! }}

        return when (result) {
            is Result.Success ->{
                SignInResult.CreateSessionFromLink.Success(result.value)
            }
            is Result.Failure -> catchException(result.error)
        }
    }

    private fun signalRegistrationOperation()
            : Result<Pair<SignalKeyGenerator.RegistrationBundles, Account>, Exception>  {
        return Result.of {
            val recipientId = if(domain != Contact.mainDomain)
                username.plus("@$domain")
            else
                username
            val registrationBundles = keyGenerator.register(recipientId,
                    randomId)
            val privateBundle = registrationBundles.privateBundle
            val account = Account(id = 0, recipientId = username, deviceId = randomId,
                    name = name, registrationId = privateBundle.registrationId,
                    identityKeyPairB64 = privateBundle.identityKeyPair, jwt = ephemeralJwt,
                    signature = "", refreshToken = "", isActive = true, domain = domain, isLoggedIn = true,
                    hasCloudBackup = false, lastTimeBackup = null, wifiOnly = true, autoBackupFrequency = 0,
                    backupPassword = null, type = AccountTypes.STANDARD, blockRemoteContent = false)
            Pair(registrationBundles, account)
        }
    }

    private val storeAccountOperation
            : (Pair<SignalKeyGenerator.RegistrationBundles, Account>) -> Result<Unit, Exception> = {
        (registrationBundles, account) ->
        Result.of {
            val postKeyBundleStep = Runnable {
                val response = apiClient.postKeybundle(bundle = registrationBundles.uploadBundle,
                        jwt = account.jwt)
                val json = JSONObject(response.body)
                account.jwt = json.getString("token")
                account.refreshToken = json.getString("refreshToken")
                if(messagingInstance.token != null)
                    apiClient.putFirebaseToken(messagingInstance.token ?: "", account.jwt)
                accountDao.updateJwt(username, domain, account.jwt)
                accountDao.updateRefreshToken(username, domain, account.refreshToken)
            }

            storeAccountTransaction.run(account = account,
                    keyBundle = registrationBundles.privateBundle,
                    extraSteps = postKeyBundleStep, isMultiple = isMultiple)
        }

    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        if(ex is ServerErrorException) UIMessage(R.string.server_bad_status, arrayOf(ex.errorCode))
        UIMessage(resId = R.string.forgot_password_error)
    }

}