package com.criptext.mail.scenes.params

import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.composer.ComposerActivity
import com.criptext.mail.scenes.composer.data.ComposerType

/**
 * Created by gabriel on 2/15/18.
 */

data class ComposerParams(val type: ComposerType, val currentLabel: Label): SceneParams() {
    override val activityClass = ComposerActivity::class.java
}