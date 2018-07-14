package com.email.scenes.composer.ui

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerUIObserver {
    fun onAttachmentButtonClicked()
    fun onAttachmentRemoveClicked(position: Int)
    fun onRecipientListChanged()
    fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean)
    fun onBackButtonClicked()
    fun onConfirmPasswordChangedListener(text: String)
    fun onPasswordChangedListener(text: String)
    fun setOnCheckedChangeListener(isChecked: Boolean)
}