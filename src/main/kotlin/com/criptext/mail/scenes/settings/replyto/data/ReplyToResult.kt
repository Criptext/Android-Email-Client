package com.criptext.mail.scenes.settings.replyto.data

import com.criptext.mail.utils.UIMessage

sealed class ReplyToResult{
    sealed class SetReplyToEmail: ReplyToResult() {
        data class Success(val replyToEmail: String, val enabled: Boolean): SetReplyToEmail()
        data class Failure(val message: UIMessage): SetReplyToEmail()
        class Forbidden: SetReplyToEmail()
        class EnterpriseSuspended: SetReplyToEmail()
    }
}