package com.criptext.mail.scenes.signin.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import org.json.JSONObject


class CreateSessionWorker(val httpClient: HttpClient,
                          private val db: SignInLocalDB,
                          private val isMultiple: Boolean,
                          signUpDao: SignUpDao,
                          private val name : String,
                          private val username: String,
                          private val accountDao: AccountDao,
                          private val keyValueStorage: KeyValueStorage,
                          private val keyGenerator: SignalKeyGenerator,
                          private val messagingInstance: MessagingInstance,
                          private val ephemeralJwt: String,
                          private val randomId: Int,
                          override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.CreateSessionFromLink> {

    private val apiClient = SignInAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(signUpDao, keyValueStorage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.CreateSessionFromLink {
        return SignInResult.CreateSessionFromLink.Failure(createErrorMessage(ex), ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.CreateSessionFromLink>): SignInResult.CreateSessionFromLink? {

        val result = Result.of {
                    val lastLoggedUser = keyValueStorage.getString(KeyValueStorage.StringKey.LastLoggedUser, "")
                    if(!isMultiple) {
                        if (lastLoggedUser.isNotEmpty())
                            db.deleteDatabase(lastLoggedUser)
                        else
                            db.deleteDatabase()
                        keyValueStorage.clearAll()
                    } else {
                        if (lastLoggedUser.isNotEmpty()) {
                            val account = accountDao.getAccountByRecipientId(lastLoggedUser)!!
                            db.deleteDatabase(account)
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
            val registrationBundles = keyGenerator.register(username,
                    randomId)
            val privateBundle = registrationBundles.privateBundle
            val account = Account(id = 0, recipientId = username, deviceId = randomId,
                    name = name, registrationId = privateBundle.registrationId,
                    identityKeyPairB64 = privateBundle.identityKeyPair, jwt = ephemeralJwt,
                    signature = "", refreshToken = "", isActive = true, domain = Contact.mainDomain, isLoggedIn = true)
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
                accountDao.updateJwt(username, account.jwt)
                accountDao.updateRefreshToken(username, account.refreshToken)
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
        UIMessage(resId = R.string.forgot_password_error)
    }

}