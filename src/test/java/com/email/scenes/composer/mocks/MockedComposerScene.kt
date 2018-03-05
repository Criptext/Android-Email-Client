package com.email.scenes.composer.mocks

import com.email.db.models.Contact
import com.email.scenes.composer.ComposerScene
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.UIData
import com.email.utils.UIMessage

/**
 * Created by gabriel on 2/27/18.
 */
class MockedComposerScene: ComposerScene {

    override fun setContactSuggestionList(contacts: Array<Contact>) {
    }

    override fun toggleExtraFieldsVisibility(visible: Boolean) {
    }

    override var observer: ComposerUIObserver? = null

    var lastError: UIMessage? = null
    var displayedData: UIData? = null

    override fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>, uiData: UIData) {
        displayedData = uiData
    }

    override fun getDataInputByUser(): UIData {
        return displayedData!!
    }

    override fun showError(message: UIMessage) {
        lastError = message
    }

}