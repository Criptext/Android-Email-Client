package com.criptext.mail.scenes.settings.changepassword

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class ChangePasswordUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onOldPasswordChangedListener(password: String)
    abstract fun onPasswordChangedListener(password: String)
    abstract fun onConfirmPasswordChangedListener(confirmPassword: String)
    abstract fun onChangePasswordButtonPressed()
    abstract fun onForgotPasswordPressed()
}