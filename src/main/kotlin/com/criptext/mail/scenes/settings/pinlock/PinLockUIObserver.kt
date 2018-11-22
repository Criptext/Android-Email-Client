package com.criptext.mail.scenes.settings.pinlock

import com.criptext.mail.utils.uiobserver.UIObserver

interface PinLockUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onPinSwitchChanged(isEnabled: Boolean)
    fun onPinChangePressed()
    fun onAutoTimeSelected(position: Int)
}