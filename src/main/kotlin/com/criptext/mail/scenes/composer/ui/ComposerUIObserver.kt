package com.criptext.mail.scenes.composer.ui

import android.view.View
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerUIObserver: UIObserver {
    fun onAttachmentButtonClicked()
    fun onNewCamAttachmentRequested()
    fun onNewFileAttachmentRequested()
    fun onNewGalleryAttachmentRequested()
    fun onAttachmentRemoveClicked(position: Int)
    fun onRecipientListChanged()
    fun onRecipientAdded()
    fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean)
    fun onBackButtonClicked()
    fun sendDialogButtonPressed()
    fun sendDialogCancelPressed()
    fun leaveComposer()
    fun showStartGuideAttachments(view: View)
    fun onSenderSelectedItem(index: Int)
}