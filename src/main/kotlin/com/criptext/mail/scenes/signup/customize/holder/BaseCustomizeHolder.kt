package com.criptext.mail.scenes.signup.customize.holder

import com.criptext.mail.scenes.signup.customize.ui.CustomizeUIObserver
import com.criptext.mail.validation.ProgressButtonState

abstract class BaseCustomizeHolder {
    var uiObserver: CustomizeUIObserver? = null
    abstract fun setSubmitButtonState(state : ProgressButtonState)
}
