package com.email.scenes.params

import com.email.scenes.mailbox.MailboxActivity
import com.email.scenes.composer.data.ComposerInputData

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
