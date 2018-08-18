package com.criptext.mail.scenes.settings.change_email

interface ChangeEmailUIObserver {
    fun onBackButtonPressed()
    fun onRecoveryEmailTextChanged(text: String)
    fun onChangeButtonPressed(text: String)
    fun onEnterPasswordOkPressed(password: String)
}