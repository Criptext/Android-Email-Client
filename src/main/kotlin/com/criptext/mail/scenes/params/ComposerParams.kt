package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.composer.ComposerActivity
import com.criptext.mail.scenes.composer.data.ComposerType

/**
 * Created by gabriel on 2/15/18.
 */

data class ComposerParams(val type: ComposerType): SceneParams() {
    override val activityClass = ComposerActivity::class.java
}