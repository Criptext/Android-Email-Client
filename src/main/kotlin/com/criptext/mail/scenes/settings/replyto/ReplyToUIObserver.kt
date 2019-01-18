package com.criptext.mail.scenes.settings.replyto


interface ReplyToUIObserver {
    fun onBackButtonPressed()
    fun onRecoveryEmailChanged(text: String)
    fun onRecoveryChangeButonPressed()
    fun onTurnOffReplyTo()
}