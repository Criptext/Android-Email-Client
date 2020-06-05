package com.criptext.mail.scenes.settings.replyto

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver


abstract class ReplyToUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onRecoveryEmailChanged(text: String)
    abstract fun onRecoveryChangeButonPressed()
    abstract fun onTurnOffReplyTo()
}