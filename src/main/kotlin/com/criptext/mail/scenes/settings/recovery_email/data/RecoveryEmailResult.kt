package com.criptext.mail.scenes.settings.recovery_email.data

import com.criptext.mail.utils.UIMessage

sealed class RecoveryEmailResult{

    sealed class ResendConfirmationLink: RecoveryEmailResult() {
        class Success: ResendConfirmationLink()
        class Failure: ResendConfirmationLink()
    }

    sealed class ChangeRecoveryEmail: RecoveryEmailResult() {
        class Success(val newEmail: String): ChangeRecoveryEmail()
        class Failure(val ex: Exception, val message: UIMessage): ChangeRecoveryEmail()
        class EnterpriseSuspended: ChangeRecoveryEmail()
    }

}