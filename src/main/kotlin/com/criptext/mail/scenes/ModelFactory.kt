package com.criptext.mail.scenes

import com.criptext.mail.scenes.composer.ComposerModel
import com.criptext.mail.scenes.composer.data.ComposerType

object ModelFactory {
    fun createComposerModel(type: ComposerType): ComposerModel = ComposerModel(type)
}