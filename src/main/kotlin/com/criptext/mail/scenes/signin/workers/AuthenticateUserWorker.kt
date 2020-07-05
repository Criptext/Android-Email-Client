package com.criptext.mail.scenes.signin.workers

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
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
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.signin.data.SignInAPIClient
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signin.data.SignInSession
import com.criptext.mail.scenes.signin.data.UserData
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.*
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by sebas on 2/28/18.
 */
class AuthenticateUserWorker(
        private val db: SignInLocalDB,
        signUpDao: SignUpDao,
        httpClient: HttpClient,
        private val isMultiple: Boolean,
        private val accountDao: AccountDao,
        private val aliasDao: AliasDao,
        private val customDomainDao: CustomDomainDao,
        private val keyValueStorage: KeyValueStorage,
        private val keyGenerator: SignalKeyGenerator,
        private val userData: UserData,
        private val messagingInstance: MessagingInstance,
        override val publishFn: (SignInResult.AuthenticateUser) -> Unit)
    : BackgroundWorker<SignInResult.AuthenticateUser> {

    private val apiClient = SignInAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(signUpDao, keyValueStorage, accountDao, aliasDao, customDomainDao)
    private lateinit var addressesJsonArray: JSONArray
    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.AuthenticateUser {
        val message = createErrorMessage(ex)
        return SignInResult.AuthenticateUser.Failure(message, ex)
    }

    private val shouldKeepData: Boolean by lazy {
        userData.username in AccountUtils.getLastLoggedAccounts(keyValueStorage) ||
        userData.username.plus("@${userData.domain}") in AccountUtils.getLastLoggedAccounts(keyValueStorage) ||
        db.getAccount(userData.username, userData.domain) != null
    }

    private fun authenticateUser(): String {
        val responseString = apiClient.authenticateUser(userData).body
        keyValueStorage.putString(KeyValueStorage.StringKey.SignInSession, responseString)
        addressesJsonArray = JSONObject(responseString).getJSONArray("addresses")
        return responseString
    }

    private fun getSignInSession(): SignInSession {
        val authenticatedUser = authenticateUser()
        var storedValue = keyValueStorage.getString(KeyValueStorage.StringKey.SignInSession, "")
        val lastLoggedUsers = AccountUtils.getLastLoggedAccounts(keyValueStorage)
        if(lastLoggedUsers.isNotEmpty()) {
            if(!shouldKeepData){
                if(isMultiple) {
                    keyValueStorage.remove(listOf(
                            KeyValueStorage.StringKey.LastLoggedUser
                    ))
                } else {
                    keyValueStorage.clearAll()
                }
                db.deleteDatabase(lastLoggedUsers)
                lastLoggedUsers.clear()
            }
            storedValue = ""
            lastLoggedUsers.removeAll { it == userData.username.plus("@${userData.domain}") }
            keyValueStorage.putString(KeyValueStorage.StringKey.LastLoggedUser, lastLoggedUsers.distinct().joinToString())
        }
        val jsonString = if (storedValue.isEmpty() || (isMultiple && !shouldKeepData)) authenticatedUser else storedValue
        val jsonObject = JSONObject(jsonString)
        return SignInSession.fromJSON(jsonObject)

    }

    private fun signInOperation(): Result<SignInSession, Exception> =
        Result.of { getSignInSession() }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private val signalRegistrationOperation
            : (SignInSession) ->
            Result<Pair<SignalKeyGenerator.RegistrationBundles, Account>, Exception> = {
        signInSession ->
        Result.of {
            val recipientId = if(userData.domain != Contact.mainDomain)
                userData.username.plus("@${userData.domain}")
            else
                userData.username
            val registrationBundles = keyGenerator.register(recipientId, signInSession.deviceId)
            val privateBundle = registrationBundles.privateBundle
            val account = Account(id = 0, recipientId = userData.username, deviceId = signInSession.deviceId,
                    name = signInSession.name, registrationId = privateBundle.registrationId,
                    identityKeyPairB64 = privateBundle.identityKeyPair, jwt = signInSession.token,
                    signature = "", refreshToken = "", isActive = true, domain = userData.domain, isLoggedIn = true,
                    autoBackupFrequency = 0, hasCloudBackup = false, lastTimeBackup = null, wifiOnly = true,
                    backupPassword = null, type = signInSession.type, blockRemoteContent = signInSession.blockRemoteContent,
                    defaultAddress = null)
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
                accountDao.updateJwt(userData.username, userData.domain, account.jwt)
                accountDao.updateRefreshToken(userData.username, userData.domain, account.refreshToken)
            }

            storeAccountTransaction.run(account = account,
                                        keyBundle = registrationBundles.privateBundle,
                                        extraSteps = postKeyBundleStep, keepData = shouldKeepData,
                                        isMultiple = isMultiple, addressesJsonArray = addressesJsonArray)
        }

    }

    override fun work(reporter: ProgressReporter<SignInResult.AuthenticateUser>): SignInResult.AuthenticateUser? {

        val result = signInOperation()
                .flatMap(signalRegistrationOperation)
                .flatMap(storeAccountOperation)

        return when(result) {
            is Result.Success -> {
                SignInResult.AuthenticateUser.Success()
            }
            is Result.Failure -> {
                keyValueStorage.remove(listOf(KeyValueStorage.StringKey.SignInSession))
                SignInResult.AuthenticateUser.Failure(
                        message = createErrorMessage(result.error),
                        exception = result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is NetworkErrorException ->
                UIMessage(resId = R.string.login_network_error_exception)
            is JSONException ->
                    UIMessage(resId = R.string.login_json_error_exception)
            is ServerErrorException ->{
                when(ex.errorCode){
                    ServerCodes.TooManyRequests -> {
                        val timeLeft = DateAndTimeUtils.getTimeInHoursAndMinutes(ex.headers?.getLong("Retry-After"))
                        if(timeLeft != null) {
                            if(timeLeft.first == 0L)
                            UIMessage(resId = R.string.too_many_requests_exception_minute,
                                    args = arrayOf(timeLeft.second))
                            else
                                UIMessage(resId = R.string.too_many_requests_exception_hour,
                                        args = arrayOf(timeLeft.first))
                        }else
                            UIMessage(resId = R.string.too_many_requests_exception_no_time_found)
                    }
                    ServerCodes.TooManyDevices ->
                        UIMessage(R.string.too_many_devices)
                    ServerCodes.BadRequest ->
                        UIMessage(R.string.password_enter_error)
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            else -> UIMessage(resId = R.string.login_fail_try_again_error_with_exception, args = arrayOf(ex.toString()))
        }
    }
}
