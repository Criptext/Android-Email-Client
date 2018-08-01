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
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.scenes.signup.IncompleteAccount
import com.criptext.mail.scenes.signup.data.SignUpResult.RegisterUser
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONException

/**
 * Sets up a new account. Generates a new key bundle and uploads it along with the user data to
 * the server. Once the server stores the new account, all keys and user data are saved to local
 * storage atomically.
 * Created by sebas on 2/26/18.
 */

class RegisterUserWorker(
        db: SignUpDao,
        keyValueStorage: KeyValueStorage,
        httpClient: HttpClient,
        private val signalKeyGenerator: SignalKeyGenerator,
        private val incompleteAccount: IncompleteAccount,
        private val messagingInstance: MessagingInstance,
        override val publishFn: (RegisterUser) -> Unit)
    : BackgroundWorker<RegisterUser> {

    override val canBeParallelized = false
    private val apiClient = SignUpAPIClient(httpClient)
    private val storeAccountTransaction = StoreAccountTransaction(db, keyValueStorage)


    override fun catchException(ex: Exception): RegisterUser {

        val message = createErrorMessage(ex)
        return RegisterUser.Failure(message, ex)
    }

    private fun postNewUserToServer(keyBundle: PreKeyBundleShareData.UploadBundle)
            : Result<String, Exception> =
            Result.of { apiClient.createUser(incompleteAccount, keyBundle) }
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)

    private fun persistNewUserData(keyBundle: SignalKeyGenerator.PrivateBundle)
            :(String) -> Result<Unit, Exception> {
        return { jwtoken: String ->
            Result.of {
                val newAccount = incompleteAccount.complete(keyBundle, jwtoken)
                if(messagingInstance.token != null)
                    apiClient.putFirebaseToken(messagingInstance.token ?: "", jwtoken)
                storeAccountTransaction.run(account = newAccount, keyBundle = keyBundle)
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
                if(ex.errorCode == 400) {
                    UIMessage(resId = R.string.duplicate_name_error_exception)
                } else {
                    UIMessage(resId = R.string.server_error_exception)
                }
            }
            is NetworkErrorException -> UIMessage(resId = R.string.network_error_exception)
            else -> UIMessage(resId = R.string.fail_register_try_again_error_exception)
        }
    }
}

