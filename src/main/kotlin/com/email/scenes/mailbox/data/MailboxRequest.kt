package com.email.scenes.mailbox.data

import com.email.db.MailFolders
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.labelChooser.SelectedLabels
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    data class GetLabels(
            val threadIds: List<String>): MailboxRequest()

    data class UpdateMailbox(
            val label: MailFolders): MailboxRequest()

    data class UpdateEmailThreadsLabelsRelations(
            val chosenLabel: MailFolders?,
            val selectedLabels: SelectedLabels?,
            val selectedEmailThreads: List<EmailThread>
            ): MailboxRequest()

    data class LoadEmailThreads(
            val label: MailFolders,
            val offset: Int,
            val oldestEmailThread: EmailThread?
            ): MailboxRequest()

    data class SendMail(val emailId: Int, val data: ComposerInputData): MailboxRequest()

    data class UpdateEmail(val emailId: Int, val response: JSONObject): MailboxRequest()
}
