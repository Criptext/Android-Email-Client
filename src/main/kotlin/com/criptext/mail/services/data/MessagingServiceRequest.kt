package com.criptext.mail.services.data

sealed class MessagingServiceRequest{

    data class RefreshPushTokenOnServer(val token: String): MessagingServiceRequest()
}