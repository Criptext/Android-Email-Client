package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.mailbox.MailboxActivity

/**
 * Created by sebas on 3/5/18.
 */

open class MailboxParams(val showWelcome: Boolean = false): SceneParams() {
    override val activityClass = MailboxActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other is MailboxParams
    }

    data class SendMail(val newMailData: ComposerInputData): MailboxParams()
}
