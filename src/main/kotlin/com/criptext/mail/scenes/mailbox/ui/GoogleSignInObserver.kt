package com.criptext.mail.scenes.mailbox.ui

import com.google.api.services.drive.Drive

interface GoogleSignInObserver {
    fun signInSuccess(drive: Drive)
    fun signInFailed()
}