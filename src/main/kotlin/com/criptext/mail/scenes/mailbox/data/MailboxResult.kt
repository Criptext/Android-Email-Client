package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.utils.UIMessage

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxResult {

    sealed class UpdateEmailThreadsLabelsRelations: MailboxResult() {
        data class Success(val threadIds: List<String>, val isStarred: Boolean): UpdateEmailThreadsLabelsRelations()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : UpdateEmailThreadsLabelsRelations()
        data class Unauthorized(val message: UIMessage) : UpdateEmailThreadsLabelsRelations()
        class Forbidden: UpdateEmailThreadsLabelsRelations()
    }

    sealed class MoveEmailThread: MailboxResult() {
        data class Success(val threadIds: List<String>, val chosenLabel: String?): MoveEmailThread()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : MoveEmailThread()
        data class Unauthorized(val message: UIMessage) : MoveEmailThread()
        class Forbidden: MoveEmailThread()
    }

    sealed class GetSelectedLabels : MailboxResult() {
        class Success(val allLabels: List<Label>,
                      val selectedLabels: List<Label>): GetSelectedLabels()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : GetSelectedLabels()
    }

    sealed class LoadEmailThreads : MailboxResult() {
        abstract fun getDestinationMailbox(): String
        class Success(
                val emailPreviews: List<EmailPreview>,
                val loadParams: LoadParams,
                val mailboxLabel: String): LoadEmailThreads() {

            override fun getDestinationMailbox(): String {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: String,
                val message: UIMessage,
                val exception: Exception) : LoadEmailThreads() {

            override fun getDestinationMailbox(): String {
                return mailboxLabel
            }
        }
    }

    sealed class SendMail: MailboxResult() {
        class Success(val emailId: Long?, val isSecure: Boolean): SendMail()
        data class Failure(val message: UIMessage): SendMail()
        data class Unauthorized(val message: UIMessage): SendMail()
        class Forbidden: SendMail()
        class EnterpriseSuspended: SendMail()
    }

    sealed class GetMenuInformation : MailboxResult() {
        data class Success(val account: Account, val totalInbox: Int, val totalDraft: Int,
                           val totalSpam: Int, val labels: List<Label>, val accounts: List<Account>): GetMenuInformation()
        class Failure: GetMenuInformation()
    }

    sealed class SetActiveAccount : MailboxResult() {
        data class Success(val activeAccount: ActiveAccount): SetActiveAccount()
        class Failure: SetActiveAccount()
    }

    sealed class SetActiveAccountFromPush : MailboxResult() {
        data class Success(val activeAccount: ActiveAccount, val extrasData: IntentExtrasData): SetActiveAccountFromPush()
        class Failure: SetActiveAccountFromPush()
    }

    sealed class UpdateUnreadStatus: MailboxResult(){
        data class Success(val threadId: List<String>, val unreadStatus: Boolean): UpdateUnreadStatus()
        class Failure(val message: UIMessage): UpdateUnreadStatus()
        class Unauthorized(val message: UIMessage): UpdateUnreadStatus()
        class Forbidden: UpdateUnreadStatus()
    }

    sealed class GetEmailPreview: MailboxResult() {
        data class Success(val emailPreview: EmailPreview,
                           val isTrash: Boolean, val isSpam: Boolean,
                           val doReply: Boolean = false,
                           val activityMessage: ActivityMessage? = null): GetEmailPreview()
        data class Failure(val message: UIMessage): GetEmailPreview()
    }

    sealed class EmptyTrash: MailboxResult() {
        class Success: EmptyTrash()
        class Failure(val message: UIMessage): EmptyTrash()
        class Unauthorized(val message: UIMessage): EmptyTrash()
        class Forbidden: EmptyTrash()
    }

    sealed class ResendEmails: MailboxResult() {
        class Success: ResendEmails()
        data class Failure(val message: UIMessage): ResendEmails()
        class EnterpriseSuspended: ResendEmails()
    }

    sealed class ResendPeerEvents: MailboxResult() {
        data class Success(val queueIsEmpty: Boolean): ResendPeerEvents()
        data class Failure(val queueIsEmpty: Boolean): ResendPeerEvents()
        data class ServerFailure(val message: UIMessage, val queueIsEmpty: Boolean): ResendPeerEvents()
    }
}
