package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.emaildetail.EmailDetailActivity
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview

/**
 * Created by sebas on 3/13/18.
 */

class EmailDetailParams(val threadId: String,
                        val currentLabel: Label,
                        val threadPreview: EmailPreview,
                        val doReply: Boolean = false): SceneParams() {
    override val activityClass = EmailDetailActivity::class.java
}
