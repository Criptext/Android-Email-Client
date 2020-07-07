package com.criptext.mail.push.data

import android.graphics.Bitmap
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.UIMessage


sealed class PushResult {

    sealed class UpdateMailbox : PushResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val mailboxLabel: Label,
                val isManual: Boolean,
                val senderImage: Bitmap?): UpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Progress(val progress: Int, val max: Int): UpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return Label.defaultItems.inbox
            }
        }

        data class SuccessAndRepeat(
                val mailboxLabel: Label,
                val isManual: Boolean,
                val senderImage: Bitmap?): UpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?): UpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }
    }

    sealed class NewEmail : PushResult() {
        data class Success(
                val mailboxLabel: Label,
                val isManual: Boolean,
                val pushData: Map<String, String>,
                val shouldPostNotification: Boolean,
                val senderImage: Bitmap?,
                val notificationId: Int,
                val activeAccount: ActiveAccount): NewEmail()

        class SilentSuccess: NewEmail()

        data class Failure(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?,
                val pushData: Map<String, String>,
                val shouldPostNotification: Boolean,
                val notificationId: Int,
                val activeAccount: ActiveAccount?): NewEmail()
    }

    sealed class LinkAccept: PushResult() {
        data class Success(val notificationId: Int): LinkAccept()
        data class Failure(val message: UIMessage): LinkAccept()
    }

    sealed class LinkDeny: PushResult() {
        data class Success(val notificationId: Int): LinkDeny()
        data class Failure(val message: UIMessage): LinkDeny()
    }

    sealed class RemoveNotification: PushResult() {
        data class Success(val notificationId: Int, val antiPushSubtype: String): RemoveNotification()
        data class Failure(val message: UIMessage): RemoveNotification()
    }
}
