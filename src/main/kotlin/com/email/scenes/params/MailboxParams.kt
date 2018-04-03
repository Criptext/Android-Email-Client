package com.email.scenes.params

import com.email.MailboxActivity
import com.email.scenes.composer.ui.UIData

/**
 * Created by sebas on 3/5/18.
 */

open class MailboxParams: SceneParams() {
    override val activityClass = MailboxActivity::class.java

    class SendMail(val newMailData: UIData): MailboxParams()
}
