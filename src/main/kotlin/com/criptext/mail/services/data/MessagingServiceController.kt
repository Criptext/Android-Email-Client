package com.criptext.mail.services.data

import com.criptext.mail.services.MessagingInstance

class MessagingServiceController(private val dataSource: MessagingServiceDataSource,
                                 private val messagingInstance: MessagingInstance){

    fun refreshPushToken(){
        if(messagingInstance.token != null){
            dataSource.submitRequest(MessagingServiceRequest.RefreshPushTokenOnServer(messagingInstance.token ?: ""))
        }
    }
}