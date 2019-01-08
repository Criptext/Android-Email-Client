package com.criptext.mail.api

import android.accounts.NetworkErrorException
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.utils.ServerCodes
import com.github.kittinunf.result.Result
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

/**
 * Utilities to handle errors in various Http requests. BackgroundWorker instances should use
 * these functions instead of implementing their own.
 * Created by gabriel on 9/20/17.
 */

object HttpErrorHandlingHelper {

    val httpExceptionsToNetworkExceptions: (Exception) -> Exception
        get() = { exception: Exception ->
            exception.printStackTrace()
            if (exception is IOException
                    || exception is NullPointerException
                    || exception is SSLException
                    || exception is SocketTimeoutException
                    || exception is ConnectException)
                NetworkErrorException()
            else
                exception
        }

    private fun isSessionExpiredError(errorCode: Int): Boolean = errorCode == ServerCodes.Unauthorized

    fun didFailBecauseInvalidSession(operation: Result<*, Exception>): Boolean {
        return if (operation is Result.Failure) {
            val error = operation.error as? ServerErrorException
            if (error != null)
                HttpErrorHandlingHelper.isSessionExpiredError(error.errorCode)
            else
                false
        } else
            false
    }

    fun newRefreshSessionOperation(apiClient: CriptextAPIClient, account: ActiveAccount, storage: KeyValueStorage,
                                   accountDao: AccountDao)
            : Result<Unit, Exception> {
        return Result.of {
            val accountInDB = accountDao.getLoggedInAccount()
            if(accountInDB != null){
                if(accountInDB.refreshToken.isEmpty()){
                    val refreshAndSession = apiClient.getRefreshToken(account.jwt).body
                    account.updateUserWithTokensData(storage, refreshAndSession)
                    accountDao.updateJwt(account.recipientId, JSONObject(refreshAndSession).getString("token"))
                    accountDao.updateRefreshToken(account.recipientId, JSONObject(refreshAndSession).getString("refreshToken"))
                }else{
                    val sessionToken = apiClient.refreshSession(account.refreshToken).body
                    account.updateUserWithSessionData(storage, sessionToken)
                    accountDao.updateJwt(account.recipientId, sessionToken)
                }
            }
        }
    }


}
