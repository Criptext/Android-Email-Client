package com.email.scenes.params

import com.email.EmailDetailActivity

/**
 * Created by sebas on 3/13/18.
 */

class EmailDetailParams(val threadId: String): SceneParams() {
    override val activityClass = EmailDetailActivity::class.java
}
