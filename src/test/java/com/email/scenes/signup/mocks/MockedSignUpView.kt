package com.email.scenes.signup.mocks

import com.email.scenes.signup.OnRecoveryEmailWarningListener
import com.email.scenes.signup.SignUpScene
import com.email.scenes.signup.SignUpSceneController
import com.email.utils.UIMessage

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpView: SignUpScene {

    override fun resetSceneWidgetsFromModel(username: String, fullName: String, password: String, recoveryEmail: String) {
    }

    override fun showFormHolder() {
    }

    override fun showKeyGenerationHolder() {
    }

    var userNameSuccess = false
    var userNameErrors = false
    var passwordSuccess = false
    var passwordErrors = false
    var btnCreateAccount = false
    var errorSignUp = false

    override var signUpListener: SignUpSceneController.SignUpListener? = null
    override fun showRecoveryEmailWarningDialog(
            onRecoveryEmailWarningListener: OnRecoveryEmailWarningListener){
    }

    private fun showUsernameSucess() {
        userNameSuccess = true
    }

    private fun hideUsernameSucess() {
        userNameSuccess = true
    }

    override fun togglePasswordSuccess(show: Boolean) {
        passwordSuccess = show
    }

    override fun togglePasswordErrors(show: Boolean) {
        passwordErrors = show
    }

    override fun disableCreateAccountButton() {
        btnCreateAccount = false
    }

    override fun enableCreateAccountButton() {
        btnCreateAccount = true
    }

    override fun isPasswordErrorShown(): Boolean {
        return passwordErrors
    }


    override fun isUsernameErrorShown(): Boolean {
        return userNameErrors
    }

    override fun isUserAvailable(userAvailable: Boolean){
        if(userAvailable) {
            showUsernameSucess()
            toggleUsernameErrors(show = false)
        } else {
            hideUsernameSucess()
            toggleUsernameErrors(show = true)
        }
    }

    override fun initListeners(signUpListener: SignUpSceneController.SignUpListener){
        this.signUpListener = signUpListener
    }

    override fun showError(message: UIMessage) {
        errorSignUp = true
    }

    override fun showSuccess() {
        errorSignUp = false
    }

    override fun toggleUsernameErrors(show: Boolean) {
        userNameErrors = show
    }
}
