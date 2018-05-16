package com.email.scenes.signup

import com.email.scenes.SceneModel
import com.email.utils.form.FormInputState
import com.email.utils.form.TextInput

/**
 * Created by sebas on 2/23/18.
 */

class SignUpSceneModel : SceneModel {
    var username: TextInput = TextInput.blank()
    var fullName: TextInput = TextInput.blank()

    // password is stored differently because both inputs share the same state
    var password: String = ""
    var confirmPassword: String = ""
    var passwordState: FormInputState = FormInputState.Unknown()

    var recoveryEmail: TextInput = TextInput.blank()
    var checkTermsAndConditions : Boolean = false
}