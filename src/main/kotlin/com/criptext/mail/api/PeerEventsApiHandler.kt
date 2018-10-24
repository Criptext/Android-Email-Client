package com.criptext.mail.api

import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.utils.PeerQueue
import com.criptext.mail.utils.ServerErrorCodes
import com.github.kittinunf.result.Result
import org.json.JSONObject

interface PeerEventsApiHandler {
    fun enqueueEvent(jsonObject: JSONObject)
    fun checkForMorePendingEvents(): Boolean
    fun dispatchEvents(): Result<Unit, Exception>

    class Default(httpClient: HttpClient, jwt: String, pendingEventDao: PendingEventDao):
            PeerEventsApiHandler{

        private val apiClient = PeerAPIClient(httpClient, jwt)
        private val queue = PeerQueue.EventQueue(apiClient, pendingEventDao)

        override fun enqueueEvent(jsonObject: JSONObject) {
            queue.enqueue(jsonObject)
        }

        override fun checkForMorePendingEvents(): Boolean {
            return queue.isEmpty()
        }

        override fun dispatchEvents(): Result<Unit, Exception> {
            val picks = queue.pick()
            return queue.dispatchAndDequeue(picks)
        }

        private fun ServerErrorException.isQueueableError(): Boolean{
            if(errorCode >= ServerErrorCodes.InternalServerError) return true
            when(errorCode){
                ServerErrorCodes.Unauthorized,
                ServerErrorCodes.Forbidden,
                ServerErrorCodes.TooManyRequests -> return true
            }
            return false
        }
    }
}
