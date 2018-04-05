package com.email.scenes.mailbox.data

import com.email.db.LabelTextTypes
import com.email.scenes.composer.data.ComposerInputData

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    class GetLabels(
            val threadIds: List<String>): MailboxRequest()

    class UpdateMailbox(
            val label: LabelTextTypes): MailboxRequest()

    class LoadEmailThreads(
            val label: LabelTextTypes,
            val offset: Int,
            val oldestEmailThread: EmailThread?
            ): MailboxRequest()

    data class SendMail(val data: ComposerInputData): MailboxRequest()
}
