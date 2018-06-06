package com.email.scenes.mailbox.data

import com.email.db.MailFolders
import com.email.db.models.Label
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.label_chooser.SelectedLabels

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    data class GetSelectedLabels(
            val threadIds: List<String>): MailboxRequest()

    data class UpdateMailbox(
            val label: Label,
            val loadedThreadsCount: Int): MailboxRequest()

    data class UpdateEmailThreadsLabelsRelations(
            val selectedLabels: SelectedLabels,
            val selectedEmailThreads: List<EmailThread>,
            val currentLabel: Label,
            val removeCurrentLabel: Boolean
            ): MailboxRequest()

    data class MoveEmailThread(
            val chosenLabel: MailFolders?,
            val selectedEmailThreads: List<EmailThread>,
            val currentLabel: Label
    ): MailboxRequest()

    data class LoadEmailThreads(
            val label: MailFolders,
            val loadParams: LoadParams
            ): MailboxRequest()

    data class SendMail(val emailId: Long, val threadId: String?, val data: ComposerInputData): MailboxRequest()

    class GetMenuInformation : MailboxRequest()

    data class UpdateUnreadStatus(val emailThreads: List<EmailThread>,
                                  val updateUnreadStatus: Boolean,
                                  val currentLabel: Label): MailboxRequest()

}
