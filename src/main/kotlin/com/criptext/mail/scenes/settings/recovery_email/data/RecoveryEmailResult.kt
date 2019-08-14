package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.utils.UIMessage

sealed class RecoveryEmailResult{

    sealed class ResendConfirmationLink: RecoveryEmailResult() {
        class Success: ResendConfirmationLink()
        data class Failure(val message: UIMessage): ResendConfirmationLink()
    }

    sealed class ChangeRecoveryEmail: RecoveryEmailResult() {
        class Success(val newEmail: String): ChangeRecoveryEmail()
        class Failure(val ex: Exception, val message: UIMessage): ChangeRecoveryEmail()
        class EnterpriseSuspended: ChangeRecoveryEmail()
    }

}