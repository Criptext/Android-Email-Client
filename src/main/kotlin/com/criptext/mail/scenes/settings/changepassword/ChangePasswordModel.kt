package com.criptext.mail.scenes.settings.changepassword

import com.criptext.mail.validation.FormInputState

class ChangePasswordModel(){
    var lastUsedPassword: String = ""

    var oldPasswordText: String = ""
    var passwordText: String = ""
    var confirmPasswordText: String = ""
    var passwordState: FormInputState = FormInputState.Unknown()
}