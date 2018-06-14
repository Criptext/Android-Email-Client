package com.email.scenes.composer.ui

import android.support.v4.app.FragmentManager

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerUIObserver {
    fun onAttachmentButtonClicked()
    fun onAttachmentRemoveClicked(position: Int)
    fun onRecipientListChanged()
    fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean)
    fun onBackButtonClicked()
}