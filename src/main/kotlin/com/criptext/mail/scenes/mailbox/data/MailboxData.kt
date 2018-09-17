package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.utils.generaldatasource.data.GeneralResult

/**
 * Created by sebas on 3/22/18.
 */

object MailboxData {

    class UpdateMailboxWorkData(val result: GeneralResult.UpdateMailbox?) {
        constructor(): this(null)
    }

    class LoadThreadsWorkData(val result: MailboxResult.LoadEmailThreads?) {
        constructor(): this(null)
    }
}
