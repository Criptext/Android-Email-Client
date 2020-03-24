package com.criptext.mail.scenes.signin.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.toList
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signin.data.SignInSession
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject


class RecoveryCodeWorker(val httpClient: HttpClient,
                         private val jwt: String,
                         private val recipientId: String,
                         private val domain: String,
                         private val code: String?,
                         private val db: AppDatabase,
                         signUpDao: SignUpDao,
                         private val keyGenerator: SignalKeyGenerator,
                         private val messagingInstance: MessagingInstance,
                         private val isMultiple: Boolean,
                         private val accountDao: AccountDao,
                         private val keyValueStorage: KeyValueStorage,
                         override val publishFn: (SignInResult) -> Unit)
    : BackgroundWorker<SignInResult.RecoveryCode> {

    private val apiClient = SignUpAPIClient(httpClient)
    private val isValidate = code != null
    private val storeAccountTransaction = StoreAccountTransaction(signUpDao, keyValueStorage, accountDao, db.aliasDao(), db.customDomainDao())
    private var emailAddress: String? = null

    override val canBeParallelized = true

    private val shouldKeepData: Boolean by lazy {
        recipientId in AccountUtils.getLastLoggedAccounts(keyValueStorage) ||
                recipientId.plus("@$domain") in AccountUtils.getLastLoggedAccounts(keyValueStorage)
    }

    override fun catchException(ex: Exception): SignInResult.RecoveryCode {
        val message = createErrorMessage(ex)
        if(!isValidate) {
            if (ex is ServerErrorException && ex.errorCode == ServerCodes.BadRequest)
                return SignInResult.RecoveryCode.Success(isValidate, emailAddress)
        }
        return SignInResult.RecoveryCode.Failure(isValidate, message, ex)
    }

    override fun work(reporter: ProgressReporter<SignInResult.RecoveryCode>): SignInResult.RecoveryCode? {
        val result = Result.of {
            if(!isValidate) {
                emailAddress = JSONObject(apiClient.postTwoFAGenerateCode(recipientId, domain, jwt).body).getString("address")
            } else {
                val json = JSONObject(apiClient.postValidateTwoFACode(recipientId, domain, jwt, code!!).body)
                val deviceId = json.getInt("deviceId")
                val name = json.getString("name")
                val addresses = json.optJSONArray("addresses")
                if(!isMultiple){
                    db.clearAllTables()
                    keyValueStorage.clearAll()
                }
                val signalPair = signalRegistrationOperation(deviceId, name)
                storeAccountOperation(signalPair.first, signalPair.second, if(addresses.toString().isEmpty()) null else addresses)
            }
        }

        return when (result) {
            is Result.Success ->{
                SignInResult.RecoveryCode.Success(isValidate, emailAddress)
            }
            is Result.Failure -> catchException(result.error)
        }

    }

    private fun signalRegistrationOperation(deviceId: Int, name: String): Pair<SignalKeyGenerator.RegistrationBundles, Account> {
        val recipient = if(domain != Contact.mainDomain)
            recipientId.plus("@${domain}")
        else
            recipientId
        val registrationBundles = keyGenerator.register(recipient, deviceId)
        val privateBundle = registrationBundles.privateBundle
        val account = Account(id = 0, recipientId = recipientId, deviceId = deviceId,
                name = name, registrationId = privateBundle.registrationId,
                identityKeyPairB64 = privateBundle.identityKeyPair, jwt = jwt,
                signature = "", refreshToken = "", isActive = true, domain = domain, isLoggedIn = true,
                autoBackupFrequency = 0, hasCloudBackup = false, lastTimeBackup = null, wifiOnly = true,
                backupPassword = null)
        return Pair(registrationBundles, account)
    }

    private fun storeAccountOperation(registrationBundles: SignalKeyGenerator.RegistrationBundles, account: Account, addressesJsonArray: JSONArray?) {
        val postKeyBundleStep = Runnable {
            val response = apiClient.postKeybundle(bundle = registrationBundles.uploadBundle,
                    jwt = account.jwt)
            val json = JSONObject(response.body)
            account.jwt = json.getString("token")
            account.refreshToken = json.getString("refreshToken")
            if(messagingInstance.token != null)
                apiClient.putFirebaseToken(messagingInstance.token ?: "", account.jwt)
            accountDao.updateJwt(recipientId, domain, account.jwt)
            accountDao.updateRefreshToken(recipientId, domain, account.refreshToken)
        }

        storeAccountTransaction.run(account = account,
                keyBundle = registrationBundles.privateBundle,
                extraSteps = postKeyBundleStep, keepData = shouldKeepData,
                isMultiple = isMultiple, addressesJsonArray = addressesJsonArray)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> {
                if (ex.errorCode == ServerCodes.MethodNotAllowed)
                    UIMessage(resId = R.string.title_warning_two_fa)
                else if(ex.errorCode == ServerCodes.BadRequest)
                    UIMessage(resId = R.string.recovery_code_dialog_error)
                else
                    UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            }
            else ->UIMessage(resId = R.string.forgot_password_error)
        }
    }

}