package com.email.scenes.signup.mocks

import com.email.scenes.signin.SignUpScene
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.OnRecoveryEmailWarningListener

/**
 * Created by sebas on 2/27/18.
 */

class MockedSignUpView: SignUpScene {
    var userNameSuccess = false
    var userNameErrors = false
    var passwordSuccess = false
    var passwordErrors = false
    var btnCreateAccount = false
    var errorSignUp = false

    private lateinit var signUpListener: SignUpSceneController.SignUpListener

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

    private fun hideUsernameErrors() {
        userNameErrors = false
    }

    override fun hidePasswordErrors() {
        passwordErrors = false
    }

    override fun showPasswordErrors() {
        passwordErrors = true
    }

    fun showUsernameErrors() {
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

    private fun assignPasswordTextListener() {
    }

    private fun assignConfirmPasswordTextChangeListener() {
    }

    private fun assignCheckTermsAndConditionsListener() {
    }

    private fun assignUsernameTextChangeListener() {
    }

    private fun assignfullNameTextChangeListener() {
    }

    private fun assignRecoveryEmailTextChangeListener() {
    }

    private fun assignTermsAndConditionsClickListener() {
    }

    private fun assignBackButtonListener() {
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

    fun assignCreateAccountClickListener() {
    }

    override fun initListeners(signUpListener: SignUpSceneController.SignUpListener){
    }

    override fun showError(message: String) {
        errorSignUp = true
    }

    override fun showSuccess(message: String) {
        errorSignUp = false
    }

}
