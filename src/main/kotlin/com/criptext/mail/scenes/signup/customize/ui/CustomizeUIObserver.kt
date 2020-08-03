package com.criptext.mail.scenes.signup.customize.ui

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class CustomizeUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity) : UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onNextButtonPressed()
    abstract fun onSkipButtonPressed()
    abstract fun onNewCamPictureRequested()
    abstract fun onNewGalleryPictureRequested()
    abstract fun onContactsSwitched(isChecked: Boolean)
    abstract fun onThemeSwitched()
}