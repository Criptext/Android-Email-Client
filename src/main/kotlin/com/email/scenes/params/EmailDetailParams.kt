package com.email.scenes.params

import com.email.scenes.emaildetail.EmailDetailActivity
import com.email.db.models.Label

/**
 * Created by sebas on 3/13/18.
 */

class EmailDetailParams(val threadId: String, val currentLabel: Label): SceneParams() {
    override val activityClass = EmailDetailActivity::class.java
}
