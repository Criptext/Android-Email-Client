package com.criptext.mail.scenes.settings.privacy

import com.criptext.mail.utils.uiobserver.UIObserver

interface PrivacyUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onTwoFASwitched(isChecked: Boolean)
    fun onReadReceiptsSwitched(isChecked: Boolean)
}