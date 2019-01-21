package com.criptext.mail.scenes.settings.replyto.data

sealed class ReplyToRequest{
    data class SetReplyToEmail(val newEmail: String, val enabled: Boolean): ReplyToRequest()
}