package com.email.scenes.params

import com.email.IHostActivity
import com.email.db.models.FullEmail
import com.email.scenes.composer.ComposerActivity
import com.email.scenes.composer.data.ComposerTypes

/**
 * Created by gabriel on 2/15/18.
 */

class ComposerParams(): SceneParams() {

    var fullEmail: FullEmail? = null
    var composerType: ComposerTypes? = null
    var userEmail: String? = null
    var emailDetailActivity: IHostActivity? = null

    constructor(fullEmail: FullEmail, composerType: ComposerTypes,
                userEmail: String, emailDetailActivity: IHostActivity?) : this() {
        this.fullEmail = fullEmail
        this.composerType = composerType
        this.userEmail = userEmail
        this.emailDetailActivity = emailDetailActivity
    }

    override val activityClass = ComposerActivity::class.java
}