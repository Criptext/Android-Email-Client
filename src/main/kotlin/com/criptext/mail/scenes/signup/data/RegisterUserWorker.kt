package com.criptext.mail.scenes.signup.data

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.scenes.signup.IncompleteAccount
import com.criptext.mail.scenes.signup.data.SignUpResult.RegisterUser
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONException
import org.json.JSONObject

/**
 * Sets up a new account. Generates a new key bundle and uploads it along with the user data to
 * the server. Once the server stores the new account, all keys and user data are saved to local
 * storage atomically.
 * Created by sebas on 2/26/18.
 */

class RegisterUserWorker(
        private val db: AppDatabase,
        private val isMultiple: Boolean,
        signUpDao: SignUpDao,
        accountDao: AccountDao,
        private val keyValueStorage: KeyValueStorage,
        httpClient: HttpClient,
        private val signalKeyGenerator: SignalKeyGenerator,
        private val incompleteAccount: IncompleteAccount,
        private val messagingInstance: MessagingInstance,
        override val publishFn: (RegisterUser) -> Unit)
    : BackgroundWorker<RegisterUser> {

    override val canBeParallelized = false
    private val apiClient = SignUpAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(signUpDao, keyValueStorage, accountDao)


    override fun catchException(ex: Exception): RegisterUser {

        val message = createErrorMessage(ex)
        return RegisterUser.Failure(message, ex)
    }

    private fun postNewUserToServer(keyBundle: PreKeyBundleShareData.UploadBundle)
            : Result<Pair<String, String>, Exception> =
            Result.of { apiClient.createUser(incompleteAccount, keyBundle) }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap { Result.of {
                    val json = JSONObject(it.body)
                    Pair(json.getString("token"), json.getString("refreshToken"))
                } }

    private fun persistNewUserData(keyBundle: SignalKeyGenerator.PrivateBundle)
            :(Pair<String, String>) -> Result<Unit, Exception> {
        return { tokens: Pair<String, String> ->
            Result.of {
                val newAccount = incompleteAccount.complete(keyBundle, tokens.first, tokens.second)
                if(messagingInstance.token != null)
                    apiClient.putFirebaseToken(messagingInstance.token ?: "", tokens.first)
                storeAccountTransaction.run(account = newAccount, keyBundle = keyBundle, isMultiple = isMultiple)
            }

        }
    }

    override fun work(reporter: ProgressReporter<RegisterUser>): RegisterUser? {
        val registrationBundle = signalKeyGenerator.register(
                recipientId = incompleteAccount.username,
                deviceId = 1)
        val operation = postNewUserToServer(registrationBundle.uploadBundle)
                          .flatMap(persistNewUserData(registrationBundle.privateBundle))

        return when(operation) {
            is Result.Success -> {
                RegisterUser.Success()
            }
            is Result.Failure -> {
                RegisterUser.Failure(
                        exception = operation.error,
                        message = createErrorMessage(operation.error))
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is JSONException -> UIMessage(resId = R.string.json_error_exception)
            is ServerErrorException -> {
                when {
                    ex.errorCode == ServerCodes.BadRequest -> UIMessage(resId = R.string.taken_username_error)
                    ex.errorCode == ServerCodes.TooManyRequests -> {
                        val timeLeft = DateAndTimeUtils.getTimeInHoursAndMinutes(ex.headers?.getLong("Retry-After"))
                        if(timeLeft != null) {
                            if(timeLeft.first == 0L)
                            UIMessage(resId = R.string.too_many_requests_exception_minute, args = arrayOf(timeLeft.second))
                            else
                            UIMessage(resId = R.string.too_many_requests_exception_hour, args = arrayOf(timeLeft.first))
                        } else
                            UIMessage(resId = R.string.too_many_requests_exception_no_time_found)
                    }
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.unknown_error, args = arrayOf(ex.toString()))
        }
    }
}

