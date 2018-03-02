package com.email.scenes.signup.mocks

import com.email.scenes.keygeneration.KeyGenerationScene
import com.email.scenes.signin.SignUpScene
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.OnRecoveryEmailWarningListener
import com.email.utils.UIMessage

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpView: SignUpScene {
    override fun getKeyGenerationScene(): KeyGenerationScene {
        return MockedKeyGenerationScene()
    }

    override fun showKeyGeneration() {
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

    override fun showPasswordSucess() {
        passwordSuccess = true
    }

    override fun hidePasswordSucess() {
        passwordSuccess = false
    }

    override fun hideUsernameErrors() {
        userNameErrors = false
    }

    override fun hidePasswordErrors() {
        passwordErrors = false
    }

    override fun showPasswordErrors() {
        passwordErrors = true
    }

    override fun showUsernameErrors() {
        userNameErrors = true
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

    override fun toggleUsernameError(userAvailable: Boolean){
        if(userAvailable) {
            showUsernameSucess()
            hideUsernameErrors()
        } else {
            hideUsernameSucess()
            showUsernameErrors()
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
}
