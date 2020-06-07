package com.criptext.mail.scenes.settings.recovery_email

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class RecoveryEmailUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onResendRecoveryLinkPressed()
    abstract fun onRecoveryEmailTextChanged(text: String)
    abstract fun onChangeButtonPressed(text: String)
    abstract fun onEnterPasswordOkPressed(password: String)
    abstract fun onForgotPasswordPressed()
}