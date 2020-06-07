package com.criptext.mail.scenes.composer.ui

import android.view.View
import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/26/18.
 */

abstract class ComposerUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onAttachmentButtonClicked()
    abstract fun onNewCamAttachmentRequested()
    abstract fun onNewFileAttachmentRequested()
    abstract fun onNewGalleryAttachmentRequested()
    abstract fun onAttachmentRemoveClicked(position: Int)
    abstract fun onRecipientListChanged()
    abstract fun onRecipientAdded()
    abstract fun onSelectedEditTextChanged(userIsEditingRecipients: Boolean)
    abstract fun onBackButtonClicked()
    abstract fun sendDialogButtonPressed()
    abstract fun sendDialogCancelPressed()
    abstract fun leaveComposer()
    abstract fun showStartGuideAttachments(view: View)
    abstract fun onSenderSelectedItem(index: Int)
}