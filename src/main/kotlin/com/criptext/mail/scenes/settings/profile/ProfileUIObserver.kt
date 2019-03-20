package com.criptext.mail.scenes.settings.profile

import com.criptext.mail.utils.uiobserver.UIObserver

interface ProfileUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onEditPicturePressed()
    fun onEditProfileNamePressed()
    fun onNewCamPictureRequested()
    fun onNewGalleryPictureRequested()
    fun onDeletePictureRequested()
    fun onProfileNameChanged(name: String)
    fun onSignatureOptionClicked()
    fun onChangePasswordOptionClicked()
    fun onRecoveryEmailOptionClicked()
    fun onReplyToChangeClicked()
    fun onLogoutClicked()
    fun onLogoutConfirmedClicked()
    fun onDeleteAccountClicked()
}