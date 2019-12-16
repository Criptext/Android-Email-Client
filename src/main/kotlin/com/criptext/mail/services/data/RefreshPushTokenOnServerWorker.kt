package com.criptext.mail.services.data

import android.accounts.NetworkErrorException
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.db.models.Account
import com.criptext.mail.scenes.signup.data.StoreAccountTransaction
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by sebas on 2/28/18.
 */
class RefreshPushTokenOnServerWorker(
        httpClient: HttpClient,
        private val pushToken: String,
        private val accountDao: AccountDao,
        override val publishFn: (MessagingServiceResult.RefreshTokenOnServer) -> Unit)
    : BackgroundWorker<MessagingServiceResult.RefreshTokenOnServer> {

    private val apiClient = MessagingServiceAPIClient(httpClient)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MessagingServiceResult.RefreshTokenOnServer {
        return MessagingServiceResult.RefreshTokenOnServer.Failure()
    }


    override fun work(reporter: ProgressReporter<MessagingServiceResult.RefreshTokenOnServer>):
            MessagingServiceResult.RefreshTokenOnServer? {

        val accounts = accountDao.getLoggedInAccounts()
        if(accounts.isNotEmpty()) {
            val result = Result.of {
               accounts.forEach { apiClient.putFirebaseToken(pushToken, it.jwt) }
            }
            return when(result) {
                is Result.Success -> {
                    MessagingServiceResult.RefreshTokenOnServer.Success(pushToken)
                }
                is Result.Failure -> {
                    MessagingServiceResult.RefreshTokenOnServer.Failure()
                }
            }
        }
        return MessagingServiceResult.RefreshTokenOnServer.Failure()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}
