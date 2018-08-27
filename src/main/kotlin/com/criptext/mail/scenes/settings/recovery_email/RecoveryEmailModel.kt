package com.criptext.mail.scenes.settings.recovery_email

data class RecoveryEmailModel(val isEmailConfirmed: Boolean, val recoveryEmail: String){
    var lastTimeConfirmationLinkSent: Long = 0
}