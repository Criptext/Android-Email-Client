package com.criptext.mail.scenes.settings.changepassword

interface ChangePasswordUIObserver {
    fun onBackButtonPressed()
    fun onOldPasswordChangedListener(password: String)
    fun onPasswordChangedListener(password: String)
    fun onConfirmPasswordChangedListener(confirmPassword: String)
    fun onChangePasswordButtonPressed()
    fun onForgotPasswordPressed()
}