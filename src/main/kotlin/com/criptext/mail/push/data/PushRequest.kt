package com.criptext.mail.push.data

import com.criptext.mail.db.models.Label


sealed class PushRequest{

    data class UpdateMailbox(
            val label: Label,
            val loadedThreadsCount: Int?,
            val pushData: Map<String, String>,
            val shouldPostNotification: Boolean): PushRequest()

    data class NewEmail(
            val label: Label,
            val pushData: Map<String, String>,
            val shouldPostNotification: Boolean): PushRequest()

    data class LinkAccept(val randomId: String, val notificationId: Int): PushRequest()
    data class LinkDenied(val randomId: String, val notificationId: Int): PushRequest()

    data class RemoveNotification(val pushData: Map<String, String>, val value: String): PushRequest()


}
