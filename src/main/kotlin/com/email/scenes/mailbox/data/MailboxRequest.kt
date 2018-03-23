package com.email.scenes.mailbox.data

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxRequest{
    class GetLabels(
            val threadIds: List<String>): MailboxRequest()

    class UpdateMailbox(
            val label: String): MailboxRequest()
}
