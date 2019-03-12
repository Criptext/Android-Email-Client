package com.criptext.mail.api

import android.content.res.Resources
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.PendingEvent
import com.criptext.mail.utils.PeerQueue
import com.criptext.mail.utils.ServerCodes
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.mapError
import org.json.JSONObject

interface PeerEventsApiHandler {
    fun enqueueEvent(jsonObject: JSONObject)
    fun checkForMorePendingEvents(): Boolean
    fun dispatchEvents(): Result<Unit, Exception>

    class Default(private val httpClient: HttpClient, private val activeAccount: ActiveAccount, pendingEventDao: PendingEventDao,
                  private val storage: KeyValueStorage, private val accountDao: AccountDao):
            PeerEventsApiHandler{

        private val apiClient = PeerAPIClient(httpClient, activeAccount.jwt)
        private val queue = PeerQueue.EventQueue(apiClient, pendingEventDao, activeAccount)

        override fun enqueueEvent(jsonObject: JSONObject) {
            queue.enqueue(jsonObject)
        }

        override fun checkForMorePendingEvents(): Boolean {
            return queue.isEmpty()
        }

        override fun dispatchEvents(): Result<Unit, Exception> {
            val picks = queue.pick()
            val op = workOperation(picks)

            val sessionExpired = HttpErrorHandlingHelper.didFailBecauseInvalidSession(op)

            return if(sessionExpired)
                newRetryWithNewSessionOperation(picks)
            else
                op
        }

        private fun ServerErrorException.isQueueableError(): Boolean{
            if(errorCode >= ServerCodes.InternalServerError) return true
            when(errorCode){
                ServerCodes.Unauthorized,
                ServerCodes.Forbidden,
                ServerCodes.TooManyRequests -> return true
            }
            return false
        }

        private fun workOperation(picks: List<PendingEvent>) : Result<Unit, Exception> =
                queue.dispatchAndDequeue(picks)

        private fun newRetryWithNewSessionOperation(picks: List<PendingEvent>)
                : Result<Unit, Exception> {
            val refreshOperation =  HttpErrorHandlingHelper.newRefreshSessionOperation(apiClient, activeAccount, storage, accountDao)
                    .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
            return when(refreshOperation){
                is Result.Success -> {
                    val account = ActiveAccount.loadFromStorage(storage)
                    if(account != null) {
                        apiClient.token = account.jwt
                        workOperation(picks)
                    }else{
                        Result.of { throw Resources.NotFoundException() }
                    }
                }
                is Result.Failure -> {
                    Result.of { throw refreshOperation.error }
                }
            }
        }
    }
}
