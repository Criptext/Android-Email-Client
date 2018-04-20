package com.email.scenes.signin

import com.email.IHostActivity
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.params.MailboxParams
import com.email.scenes.params.SignUpParams
import com.email.scenes.signin.data.SignInDataSource
import com.email.scenes.signin.data.SignInRequest
import com.email.scenes.signin.data.SignInResult

/**
 * Created by sebas on 2/15/18.
 */

class SignInSceneController(
        private val model: SignInSceneModel,
        private val scene: SignInScene,
        private val host: IHostActivity,
        private val dataSource: SignInDataSource): SceneController() {

    override val menuResourceId: Int? = null

    private val dataSourceListener = { result: SignInResult ->
        when (result) {
            is SignInResult.VerifyUser -> onVerifyUser(result)
            is SignInResult.AuthenticateUser -> onUserAuthenticated(result)
        }
    }

    private fun onUserAuthenticated(result: SignInResult.AuthenticateUser) {
        when (result) {
            is SignInResult.AuthenticateUser.Success -> {
                uiObserver.onUserAuthenticated()
            }
            is SignInResult.AuthenticateUser.Failure -> {
                scene.showError(result.message)
            }
        }
    }


    val onPasswordLoginDialogListener = object : OnPasswordLoginDialogListener {
        override fun acceptPasswordLogin() {
            scene.showPasswordLoginHolder(model.username)
        }

        override fun cancelPasswordLogin() {
        }
    }
    private fun onVerifyUser(result: SignInResult.VerifyUser) {
        scene.toggleLoginProgressBar(isLoggingIn = false)
        when (result) {
            is SignInResult.VerifyUser.Success -> {
                showLoginValidationHolder()
            }
            is SignInResult.VerifyUser.Failure -> {
                scene.drawError()
            }
        }
    }
    private val uiObserver = object : SignInUIObserver {
        override fun onForgotPasswordClick() {
            TODO("GO TO FORGOT PASSWORD???")
        }

        override fun onUserAuthenticated() {
            host.goToScene(MailboxParams(), false)
        }

        override fun onPasswordLoginClick() {
            val req = SignInRequest.AuthenticateUser(
                    username = model.username,
                    password = model.password,
                    deviceId = 1 // (?) wtf
            )
            dataSource.submitRequest(req)
        }

        override fun onPasswordChangeListener(password: String) {
            model.password = password
            if(model.password.isNotEmpty()) {
                scene.toggleConfirmButton(activated = true)
            } else {
                scene.toggleConfirmButton(activated = false)
            }
        }

        override fun onCantAccessDeviceClick(){
            scene.showPasswordLoginDialog(onPasswordLoginDialogListener)
        }
        override fun userLoginReady() {
            host.goToScene(MailboxParams(), false)
        }

        override fun onLoginClick() {
            validateUsername(model.username)
        }

        override fun toggleUsernameFocusState(isFocused: Boolean) {
        }

        override fun onUsernameTextChanged(text: String) {
            scene.drawNormalSignInOptions()
            model.username = text
        }

        override fun onSignUpLabelClicked() {
            host.goToScene(SignUpParams(), false)
        }

        override fun toggleSignUpPressedState(isPressed: Boolean) {
            scene.toggleSignUpPressed(isPressed)
        }
    }

    fun validateUsername(username: String) {

        scene.toggleLoginProgressBar(isLoggingIn = true)
        val req = SignInRequest.VerifyUser(
                username = username
        )
        dataSource.submitRequest(req)
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        scene.initListeners(signInUIObserver = uiObserver)

        return false
    }

    override fun onStop() {
        scene.signInUIObserver = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    private fun showLoginValidationHolder() {
        scene.showLoginValidationHolder()
    }

    interface SignInUIObserver {
        fun onLoginClick()
        fun toggleUsernameFocusState(isFocused: Boolean)
        fun onUsernameTextChanged(text: String)
        fun toggleSignUpPressedState(isPressed: Boolean)
        fun onSignUpLabelClicked()
        fun userLoginReady()
        fun onCantAccessDeviceClick()
        fun onPasswordLoginClick()
        fun onPasswordChangeListener(password: String)
        fun onUserAuthenticated()
        fun onForgotPasswordClick()
    }
}