package com.email.scenes.composer

import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.UIData
import com.email.utils.UIMessage


/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    fun bindWithModel(uiData: UIData)
    fun getDataInputByUser(): UIData
    fun showError(message: UIMessage)
}