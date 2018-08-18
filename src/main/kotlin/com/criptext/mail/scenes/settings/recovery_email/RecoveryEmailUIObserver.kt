package com.criptext.mail.scenes.settings.recovery_email

interface RecoveryEmailUIObserver {
    fun onBackButtonPressed()
    fun onResendRecoveryLinkPressed()
    fun onChangeRecoveryEmailPressed()
    fun onChangeEmailPasswordEnteredOkPressed(password: String)
    fun onChangeEmailNewEmailEnteredOkPressed()
}