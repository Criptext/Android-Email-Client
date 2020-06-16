package com.criptext.mail.scenes.settings.recovery_email.data

sealed class RecoveryEmailRequest{
    data class ChangeRecoveryEmail(val password: String?, val newRecoveryEmail: String): RecoveryEmailRequest()
}