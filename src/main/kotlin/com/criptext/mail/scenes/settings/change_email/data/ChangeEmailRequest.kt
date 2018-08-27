package com.criptext.mail.scenes.settings.change_email.data

sealed class ChangeEmailRequest{
    data class ChangeRecoveryEmail(val password: String, val newRecoveryEmail: String): ChangeEmailRequest()
}