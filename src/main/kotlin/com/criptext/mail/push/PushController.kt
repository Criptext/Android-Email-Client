package com.criptext.mail.push

import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.data.PushRequest
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.services.DecryptionService

class PushController(private val dataSource: PushDataSource, private val host: DecryptionService,
                     private val storage: KeyValueStorage) {

    private val dataSourceListener = { result: PushResult ->
        when (result) {
            is PushResult.UpdateMailbox -> onUpdateMailbox(result)
            is PushResult.RemoveNotification -> onRemoveNotification(result)
        }
    }

    private var realProgress = 0

    init {
        dataSource.listener = dataSourceListener
    }

    fun doGetEvents(){
        dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox, host.getUnableToDecryptLocalized()))
    }

    fun removeNotification(pushData: Map<String, String>, value: String){
        dataSource.submitRequest(PushRequest.RemoveNotification(pushData, value))
    }

    private fun onRemoveNotification(result: PushResult.RemoveNotification){
        when(result){
            is PushResult.RemoveNotification.Success -> {
                when(result.antiPushSubtype) {
                    "delete_new_email" -> {
                        host.cancelPush(result.notificationId, storage,
                                KeyValueStorage.StringKey.NewMailNotificationCount, CriptextNotification.INBOX_ID)
                    }
                    "delete_sync_link" -> {
                        host.cancelPush(result.notificationId, storage,
                                KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
                    }
                }
            }
        }
    }

    private fun onUpdateMailbox(result: PushResult.UpdateMailbox){
        when(result){
            is PushResult.UpdateMailbox.SuccessAndRepeat -> {
                dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox, host.getUnableToDecryptLocalized()))
                realProgress += EVENT_BATCH
            }
            is PushResult.UpdateMailbox.Progress -> {
                host.updateServiceProgress(realProgress + result.progress, result.max)
            }
            else -> {
                realProgress = 0
                host.checkQueuedEvents()
            }
        }
    }

    companion object{
        private const val EVENT_BATCH = 50
    }
}