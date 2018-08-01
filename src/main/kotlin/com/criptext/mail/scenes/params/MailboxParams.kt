package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.scenes.composer.data.ComposerInputData

/**
 * Created by sebas on 3/5/18.
 */

open class MailboxParams: SceneParams() {
    override val activityClass = MailboxActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other is MailboxParams
    }

    data class SendMail(val newMailData: ComposerInputData): MailboxParams()
}
