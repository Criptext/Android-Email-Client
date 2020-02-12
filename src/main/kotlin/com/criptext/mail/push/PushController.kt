package com.criptext.mail.push

import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.data.PushRequest
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.push.services.PushReceiverIntentService

class PushController(private val dataSource: PushDataSource, private val host: PushReceiverIntentService) {

    private val dataSourceListener = { result: PushResult ->
        when (result) {
            is PushResult.UpdateMailbox -> onUpdateMailbox(result)
        }
    }

    init {
        dataSource.listener = dataSourceListener
    }

    fun updateMailbox(pushData: Map<String, String>, shouldPostNotification: Boolean){
        dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox, null,
                pushData, shouldPostNotification))
    }

    private fun onUpdateMailbox(result: PushResult.UpdateMailbox){
        when(result){
            is PushResult.UpdateMailbox.Success -> {
                host.stopIntentService()
            }
            is PushResult.UpdateMailbox.SuccessAndRepeat -> {
                dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox, null,
                        result.pushData, result.shouldPostNotification))
            }
            is PushResult.UpdateMailbox.Failure -> {
                host.stopIntentService()
            }
        }
    }
}