package com.email.scenes.signin

import com.email.IHostActivity
import com.email.scenes.SceneController
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
        }
    }

    private fun onVerifyUser(result: SignInResult.VerifyUser) {
        scene.toggleLoginProgressBar(isLoggingIn = false)
        when (result) {
            is SignInResult.VerifyUser.Success -> {
                launchConnectionScene()
            }
            is SignInResult.VerifyUser.Failure -> {
                scene.drawError()
            }
        }
    }
    private val signInListener = object : SignInListener {
        override fun onLoginClick() {
            validateUsername(model.username)
        }

        override fun toggleUsernameFocusState(isFocused: Boolean) {
        }

        override fun onUsernameTextChanged(text: String) {
            scene.drawNormalSignInOptions()
            model.username = text
        }

        override fun goToSignUp() {
            host.goToScene(SignUpParams())
        }

        override fun toggleSignUpPressedState(isPressed: Boolean) {
            scene.toggleSignUpPressed(isPressed)
        }
    }

    private val progressSignInListener = object : ProgressSignInListener{
        override fun onFinish() {
            scene.toggleLoginProgressBar(isLoggingIn = false)
        }
    }

    fun validateUsername(username: String) {

        scene.toggleLoginProgressBar(isLoggingIn = true)
        val req = SignInRequest.VerifyUser(
                username = username
        )
        dataSource.submitRequest(req)
    }

    override fun onStart() {
        dataSource.listener = dataSourceListener
        scene.initListeners(signInListener = signInListener)
    }

    override fun onStop() {
        scene.signInListener = null
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    private fun launchConnectionScene() {
        scene.showConnectionHolder()
        scene.startAnimation()
    }

    interface SignInListener {
        fun onLoginClick()
        fun toggleUsernameFocusState(isFocused: Boolean)
        fun onUsernameTextChanged(text: String)
        fun toggleSignUpPressedState(isPressed: Boolean)
        fun goToSignUp()
    }
}