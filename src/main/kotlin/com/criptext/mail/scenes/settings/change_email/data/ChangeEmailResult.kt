package com.criptext.mail.scenes.settings.change_email.data

import com.criptext.mail.utils.UIMessage

sealed class ChangeEmailResult{

    sealed class ChangeRecoveryEmail: ChangeEmailResult() {
        class Success(val newEmail: String): ChangeRecoveryEmail()
        class Failure(val message: UIMessage): ChangeRecoveryEmail()
    }

}