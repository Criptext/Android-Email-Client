package com.criptext.mail.scenes.settings.replyto

import com.criptext.mail.utils.uiobserver.UIObserver


interface ReplyToUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onRecoveryEmailChanged(text: String)
    fun onRecoveryChangeButonPressed()
    fun onTurnOffReplyTo()
}