package com.email.scenes.signin.data

import android.accounts.NetworkErrorException
import com.email.R
import com.email.api.HttpClient
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao
import com.email.db.models.Account
import com.email.scenes.signup.data.StoreAccountTransaction
import com.email.signal.SignalKeyGenerator
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by sebas on 2/28/18.
 */
class AuthenticateUserWorker(
        db: SignUpDao,
        httpClient: HttpClient,
        private val keyValueStorage: KeyValueStorage,
        private val keyGenerator: SignalKeyGenerator,
        private val username: String,
        private val password: String,
        override val publishFn: (SignInResult.AuthenticateUser) -> Unit)
    : BackgroundWorker<SignInResult.AuthenticateUser> {

    private val apiClient = SignInAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(db, keyValueStorage)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): SignInResult.AuthenticateUser {
        val message = createErrorMessage(ex)
        return SignInResult.AuthenticateUser.Failure(message, ex)
    }

    private fun authenticateUser(): String {
        val responseString = apiClient.authenticateUser(username, password)
        keyValueStorage.putString(KeyValueStorage.StringKey.SignInSession, responseString)
        return responseString
    }

    private fun getSignInSession(): SignInSession {
        val storedValue = keyValueStorage.getString(KeyValueStorage.StringKey.SignInSession, "")
        val jsonString = if (storedValue.isEmpty()) authenticateUser() else storedValue
        val jsonObject = JSONObject(jsonString)
        return SignInSession.fromJSON(jsonObject)

    }

    fun signInOperation(): Result<SignInSession, Exception> =
        Result.of { getSignInSession() }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    val signalRegistrationOperation
            : (SignInSession) ->
            Result<Pair<SignalKeyGenerator.RegistrationBundles, Account>, Exception> = {
        signInSession ->
        Result.of {
            val registrationBundles = keyGenerator.register(username, signInSession.deviceId)
            val privateBundle = registrationBundles.privateBundle
            val account = Account(recipientId = username, deviceId = signInSession.deviceId,
                    name = signInSession.name, registrationId = privateBundle.registrationId,
                    identityKeyPairB64 = privateBundle.identityKeyPair, jwt = signInSession.token)
            Pair(registrationBundles, account)
        }
    }

    val storeAccountOperation
            : (Pair<SignalKeyGenerator.RegistrationBundles, Account>) -> Result<Unit, Exception> = {
        (registrationBundles, account) ->
        Result.of {
            val postKeyBundleStep = Runnable {
                apiClient.postKeybundle(bundle = registrationBundles.uploadBundle,
                        jwt = account.jwt)
            }

            storeAccountTransaction.run(account = account,
                                        keyBundle = registrationBundles.privateBundle,
                                        extraSteps = postKeyBundleStep)
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
                SignInResult.AuthenticateUser.Failure(
                        message = createErrorMessage(result.error),
                        exception = result.error)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is NetworkErrorException ->
                UIMessage(resId = R.string.login_network_error_exception)
            is JSONException ->
                    UIMessage(resId = R.string.login_json_error_exception)
            else -> UIMessage(resId = R.string.login_fail_try_again_error_exception)
        }
    }
}
