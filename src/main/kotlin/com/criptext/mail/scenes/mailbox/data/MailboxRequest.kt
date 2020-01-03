package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.label_chooser.SelectedLabels

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    data class GetSelectedLabels(
            val threadIds: List<String>): MailboxRequest()

    data class UpdateEmailThreadsLabelsRelations(
            val selectedLabels: SelectedLabels,
            val selectedThreadIds: List<String>,
            val currentLabel: Label,
            val shouldRemoveCurrentLabel: Boolean
            ): MailboxRequest()


    data class MoveEmailThread(
            val chosenLabel: String?,
            val selectedThreadIds: List<String>,
            val currentLabel: Label
    ): MailboxRequest()

    data class LoadEmailThreads(
            val label: String,
            val filterUnread: Boolean,
            val loadParams: LoadParams,
            val userEmail: String
            ): MailboxRequest()

    data class SendMail(val emailId: Long,
                        val threadId: String?,
                        val currentLabel: Label,
                        val data: ComposerInputData,
                        val attachments: List<ComposerAttachment>,
                        val fileKey: String?,
                        val senderAccount: ActiveAccount? = null): MailboxRequest()

    class GetMenuInformation : MailboxRequest()

    data class SetActiveAccount(val account: Account) : MailboxRequest()

    data class UpdateUnreadStatus(val threadIds: List<String>,
                                  val updateUnreadStatus: Boolean,
                                  val currentLabel: Label): MailboxRequest()

    class EmptyTrash: MailboxRequest()

    class ResendEmails: MailboxRequest()

    class ResendPeerEvents: MailboxRequest()
}
