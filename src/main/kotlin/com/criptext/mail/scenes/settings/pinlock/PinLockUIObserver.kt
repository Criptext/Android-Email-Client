package com.criptext.mail.scenes.settings.pinlock

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class PinLockUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onPinSwitchChanged(isEnabled: Boolean)
    abstract fun onPinChangePressed()
    abstract fun onAutoTimeSelected(position: Int)
}