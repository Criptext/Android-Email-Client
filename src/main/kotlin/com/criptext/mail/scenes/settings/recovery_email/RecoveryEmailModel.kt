package com.criptext.mail.scenes.settings.recovery_email

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.validation.TextInput

data class RecoveryEmailModel(var isEmailConfirmed: Boolean, var recoveryEmail: String): SceneModel{
    var lastTimeConfirmationLinkSent: Long = 0
    var newRecoveryEmail: TextInput = TextInput.blank()
}