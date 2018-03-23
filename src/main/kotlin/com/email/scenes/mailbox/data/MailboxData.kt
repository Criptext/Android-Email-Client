package com.email.scenes.mailbox.data

/**
 * Created by sebas on 3/22/18.
 */

object MailboxData {

    class UpdateMailboxWorkData(val result: MailboxResult.UpdateMailbox?) {
        constructor(): this(null)
    }

    class LoadThreadsWorkData(val result: MailboxResult.LoadThreads?) {
        constructor(): this(null)
    }

    var updateMailboxWorkData: UpdateMailboxWorkData? = null
    var loadThreadsWorkData: LoadThreadsWorkData? = null
}
