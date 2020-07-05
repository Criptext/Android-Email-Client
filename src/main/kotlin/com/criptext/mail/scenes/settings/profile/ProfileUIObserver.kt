package com.criptext.mail.scenes.settings.profile

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class ProfileUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onEditPicturePressed()
    abstract fun onEditProfileNamePressed()
    abstract fun onNewCamPictureRequested()
    abstract fun onNewGalleryPictureRequested()
    abstract fun onDeletePictureRequested()
    abstract fun onProfileNameChanged(name: String)
    abstract fun onSignatureOptionClicked()
    abstract fun onChangePasswordOptionClicked()
    abstract fun onRecoveryEmailOptionClicked()
    abstract fun onReplyToChangeClicked()
    abstract fun onLogoutClicked()
    abstract fun onLogoutConfirmedClicked()
    abstract fun onDeleteAccountClicked()
    abstract fun onCriptextFooterSwitched(isChecked: Boolean)
}