package com.criptext.mail.scenes.settings.changepassword

import com.criptext.mail.utils.uiobserver.UIObserver

interface ChangePasswordUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onOldPasswordChangedListener(password: String)
    fun onPasswordChangedListener(password: String)
    fun onConfirmPasswordChangedListener(confirmPassword: String)
    fun onChangePasswordButtonPressed()
    fun onForgotPasswordPressed()
}