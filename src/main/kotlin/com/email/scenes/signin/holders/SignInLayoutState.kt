package com.email.scenes.signin.holders

import com.email.validation.ProgressButtonState

/**
 * Created by gabriel on 5/18/18.
 */
sealed class SignInLayoutState {
    data class Start(val username: String): SignInLayoutState()
    data class InputPassword(val username: String, val password: String,
                             val buttonState: ProgressButtonState): SignInLayoutState()
    class WaitForApproval : SignInLayoutState()
}