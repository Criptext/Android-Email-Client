package com.email.scenes

import com.email.scenes.composer.ComposerModel
import com.email.scenes.composer.data.ComposerType

object ModelFactory {
    fun createComposerModel(type: ComposerType): ComposerModel = ComposerModel(type)
}