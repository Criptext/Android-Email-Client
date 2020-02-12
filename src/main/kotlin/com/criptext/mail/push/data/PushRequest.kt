package com.criptext.mail.push.data

import com.criptext.mail.db.models.Label


sealed class PushRequest{

    data class UpdateMailbox(
            val label: Label,
            val loadedThreadsCount: Int?,
            val pushData: Map<String, String>,
            val shouldPostNotification: Boolean): PushRequest()
}
