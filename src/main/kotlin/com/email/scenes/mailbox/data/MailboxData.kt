package com.email.scenes.mailbox.data

/**
 * Created by sebas on 3/22/18.
 */

object MailboxData {

    class UpdateMailboxWorkData(val result: MailboxResult.UpdateMailbox?) {
        constructor(): this(null)
    }

    class LoadThreadsWorkData(val result: MailboxResult.LoadEmailThreads?) {
        constructor(): this(null)
    }
}
