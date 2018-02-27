package com.email.scenes.signup

import com.email.scenes.SceneModel

/**
 * Created by sebas on 2/23/18.
 */

class SignUpSceneModel : SceneModel {
    var username : String = ""
    var fullName : String = ""
    var password : String = ""
    var confirmPassword : String = ""
    var recoveryEmail : String = ""
    var checkTermsAndConditions : Boolean = false
    var errors = HashMap<String, Boolean>()
}