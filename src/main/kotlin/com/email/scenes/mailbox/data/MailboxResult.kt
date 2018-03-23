package com.email.scenes.mailbox.data

import com.email.db.models.Label
import com.email.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxResult {

    sealed class GetLabels : MailboxResult() {
        class Success(val labels: List<Label>,
                      val defaultSelectedLabels: List<Label>): GetLabels()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : GetLabels()
    }

    sealed class UpdateMailbox : MailboxResult() {
        abstract fun getDestinationMailbox(): String
        data class Success(
                val mailboxLabel: String,
                val mailboxThreads: List<EmailThread>,
                val isManual: Boolean): UpdateMailbox() {
            override fun getDestinationMailbox(): String {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: String,
                val message: UIMessage ): UpdateMailbox() {
            override fun getDestinationMailbox(): String {
                return mailboxLabel
            }
        }
    }

    sealed class LoadThreads {

    }
}

