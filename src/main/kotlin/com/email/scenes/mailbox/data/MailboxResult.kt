package com.email.scenes.mailbox.data

import com.email.db.MailFolders
import com.email.db.models.Account
import com.email.db.models.Label
import com.email.utils.UIMessage
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxResult {

    sealed class UpdateEmailThreadsLabelsRelations: MailboxResult() {
        class Success: UpdateEmailThreadsLabelsRelations()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : UpdateEmailThreadsLabelsRelations()
    }

    sealed class MoveEmailThread: MailboxResult() {
        class Success: MoveEmailThread()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : MoveEmailThread()
    }

    sealed class GetSelectedLabels : MailboxResult() {
        class Success(val allLabels: List<Label>,
                      val selectedLabels: List<Label>): GetSelectedLabels()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : GetSelectedLabels()
    }

    sealed class LoadEmailThreads : MailboxResult() {
        abstract fun getDestinationMailbox(): MailFolders
        class Success(
                val emailThreads: List<EmailThread>,
                val isReset: Boolean,
                val mailboxLabel: MailFolders): LoadEmailThreads() {

            override fun getDestinationMailbox(): MailFolders {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: MailFolders,
                val message: UIMessage,
                val exception: Exception) : LoadEmailThreads() {

            override fun getDestinationMailbox(): MailFolders {
                return mailboxLabel
            }
        }
    }

    sealed class UpdateMailbox : MailboxResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val mailboxLabel: Label,
                val mailboxThreads: List<EmailThread>?,
                val isManual: Boolean): UpdateMailbox() {

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

    sealed class SendMail: MailboxResult() {
        class Success(val emailId: Long): SendMail()
        data class Failure(val message: UIMessage): SendMail()
    }

    sealed class GetMenuInformation : MailboxResult() {
        data class Success(val account: Account, val totalInbox: Int, val totalDraft: Int,
                           val totalSpam: Int): GetMenuInformation()
        class Failure: GetMenuInformation()
    }

    sealed class UpdateUnreadStatus: MailboxResult(){
        class Success: UpdateUnreadStatus()
        class Failure: UpdateUnreadStatus()
    }
}
