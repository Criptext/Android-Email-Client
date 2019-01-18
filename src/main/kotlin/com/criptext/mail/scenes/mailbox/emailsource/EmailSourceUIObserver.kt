package com.criptext.mail.scenes.mailbox.emailsource

import com.criptext.mail.utils.uiobserver.UIObserver

interface EmailSourceUIObserver: UIObserver {
    fun onBackButtonPressed()
}