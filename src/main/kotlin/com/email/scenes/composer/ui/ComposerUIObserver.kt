package com.email.scenes.composer.ui

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerUIObserver {
    fun onAttachmentButtonClicked()
    fun onRecipientListChanged()
    fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean)
}