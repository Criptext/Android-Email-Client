package com.criptext.mail.scenes.settings.recovery_email

interface RecoveryEmailUIObserver {
    fun onBackButtonPressed()
    fun onResendRecoveryLinkPressed()
    fun onRecoveryEmailTextChanged(text: String)
    fun onChangeButtonPressed(text: String)
    fun onEnterPasswordOkPressed(password: String)
    fun onForgotPasswordPressed()
}