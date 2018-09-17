package com.criptext.mail.scenes.settings.recovery_email

import com.criptext.mail.utils.uiobserver.UIObserver

interface RecoveryEmailUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onResendRecoveryLinkPressed()
    fun onRecoveryEmailTextChanged(text: String)
    fun onChangeButtonPressed(text: String)
    fun onEnterPasswordOkPressed(password: String)
    fun onForgotPasswordPressed()
}