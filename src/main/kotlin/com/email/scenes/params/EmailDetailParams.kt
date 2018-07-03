package com.email.scenes.params

import com.email.scenes.emaildetail.EmailDetailActivity
import com.email.db.models.Label
import com.email.email_preview.EmailPreview

/**
 * Created by sebas on 3/13/18.
 */

class EmailDetailParams(val threadId: String,
                        val currentLabel: Label,
                        val threadPreview: EmailPreview): SceneParams() {
    override val activityClass = EmailDetailActivity::class.java
}
