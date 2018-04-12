package com.email.scenes.mailbox.data

import com.email.db.LabelTextTypes
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.labelChooser.SelectedLabels
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    class GetLabels(
            val threadIds: List<String>): MailboxRequest()

    class UpdateMailbox(
            val label: LabelTextTypes): MailboxRequest()

    class UpdateEmailThreadsLabelsRelations(
            val chosenLabel: LabelTextTypes?,
                val selectedLabels: SelectedLabels?,
                val selectedEmailThreads: List<EmailThread>
            ): MailboxRequest()

    class LoadEmailThreads(
            val label: LabelTextTypes,
            val offset: Int,
            val oldestEmailThread: EmailThread?
            ): MailboxRequest()

    data class SendMail(val emailId: Int, val data: ComposerInputData): MailboxRequest()

    class UpdateEmail(val emailId: Int, val response: JSONObject): MailboxRequest()
}
