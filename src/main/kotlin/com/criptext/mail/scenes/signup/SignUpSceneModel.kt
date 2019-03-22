package com.criptext.mail.scenes.signup

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput

/**
 * Created by sebas on 2/23/18.
 */

class SignUpSceneModel(val isMultiple: Boolean = false) : SceneModel {
    var username: TextInput = TextInput.blank()
    var fullName: TextInput = TextInput.blank()

    // password is stored differently because both inputs share the same state
    var password: String = ""
    var confirmPassword: String = ""
    var passwordState: FormInputState = FormInputState.Unknown()

    var recoveryEmail: TextInput = TextInput.blank()
    var checkTermsAndConditions : Boolean = false
    var signUpSucceed: Boolean = false
}