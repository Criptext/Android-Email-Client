package com.email.scenes.params

import com.email.scenes.composer.ComposerActivity
import com.email.scenes.composer.data.ComposerType

/**
 * Created by gabriel on 2/15/18.
 */

data class ComposerParams(val type: ComposerType): SceneParams() {
    override val activityClass = ComposerActivity::class.java
}