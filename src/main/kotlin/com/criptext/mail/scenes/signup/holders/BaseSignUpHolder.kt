package com.criptext.mail.scenes.signup.holders

import com.criptext.mail.scenes.signup.SignUpSceneController
import com.criptext.mail.validation.ProgressButtonState

abstract class BaseSignUpHolder {
    var uiObserver: SignUpSceneController.SignUpUIObserver? = null
    abstract fun setSubmitButtonState(state : ProgressButtonState)
}
