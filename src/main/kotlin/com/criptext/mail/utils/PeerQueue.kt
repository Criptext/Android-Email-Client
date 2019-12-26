package com.criptext.mail.utils

import com.criptext.mail.api.PeerAPIClient
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.PendingEvent
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONArray
import org.json.JSONObject

abstract class PeerQueue{
    abstract fun enqueue(jsonObject: JSONObject)
    abstract fun pick(batchSize: Int = QUEUE_BATCH_SIZE): List<PendingEvent>
    abstract fun dequeue(ids: List<Long>)
    abstract fun dispatchAndDequeue(picks: List<PendingEvent>): Result<Unit, Exception>
    abstract fun isEmpty(): Boolean
    companion object {
        protected const val QUEUE_BATCH_SIZE = 100
    }

    class EventQueue(private val apiClient: PeerAPIClient,
                     private val pendingEventDao: PendingEventDao,
                     private val activeAccount: ActiveAccount): PeerQueue(){
        private var isBusy = false
        val isProcessing: Boolean get() = isBusy


        override fun enqueue(jsonObject: JSONObject) {
            val peerEvent = PendingEvent(0, jsonObject.toString(), activeAccount.id)
            pendingEventDao.insert(peerEvent)
            dispatchAndDequeue(pick())
        }

        override fun pick(batchSize: Int): List<PendingEvent> {
            return pendingEventDao.getByBatch(batchSize, activeAccount.id)
        }

        override fun dequeue(ids: List<Long>) {
            pendingEventDao.deleteByBatch(ids, activeAccount.id)
        }

        override fun isEmpty(): Boolean {
            return pick().isEmpty()
        }

        override fun dispatchAndDequeue(picks: List<PendingEvent>): Result<Unit, Exception> {
            isBusy = true
            val array = JSONArray(picks.map { it.data })
            val jsonObj = JSONObject()
            jsonObj.put("peerEvents", array)
            val op =  Result.of {
                if(picks.isEmpty())
                    throw EventHelper.NothingNewException()
                apiClient.postPeerEvents(jsonObj)
            }
            .flatMap { _ -> Result.of { dequeue(picks.map { it.id }) } }
            isBusy = false
            return op
        }
    }
}