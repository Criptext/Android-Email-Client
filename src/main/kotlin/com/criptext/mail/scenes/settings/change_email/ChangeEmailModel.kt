package com.criptext.mail.scenes.settings.change_email

import com.criptext.mail.validation.TextInput

data class ChangeEmailModel(var recoveryEmail: String, var isConfirmed: Boolean){
    var newRecoveryEmail: TextInput = TextInput.blank()
}