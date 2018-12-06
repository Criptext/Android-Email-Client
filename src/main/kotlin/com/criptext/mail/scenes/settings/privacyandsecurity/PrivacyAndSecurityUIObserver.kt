package com.criptext.mail.scenes.settings.privacyandsecurity

import com.criptext.mail.utils.uiobserver.UIObserver

interface PrivacyAndSecurityUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onPinSwitchChanged(isEnabled: Boolean)
    fun onPinChangePressed()
    fun onAutoTimeSelected(position: Int)
    fun onReadReceiptsSwitched(isChecked: Boolean)
    fun onEmailPreviewSwitched(isChecked: Boolean)
}