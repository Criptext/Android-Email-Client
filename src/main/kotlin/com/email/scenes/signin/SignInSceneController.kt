package com.email.scenes.signin

import android.view.Menu
import com.email.IHostActivity
import com.email.scenes.SceneController
import com.email.scenes.params.SignUpParams
import com.email.scenes.signin.data.SignInDataSource

/**
 * Created by sebas on 2/15/18.
 */

class SignInSceneController(
        private val model: SignInSceneModel,
        private val scene: SignInScene,
        private val host: IHostActivity,
        private val dataSource: SignInDataSource): SceneController() {

    override val menuResourceId: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

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
        if(username == "sebas") {
            scene.drawError()
        } else {
            TODO("SIGN IN, START PROGRESS DIALOG.")
        }
    }

    override fun onStart() {
        scene.initListeners(signInListener = signInListener)
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    interface SignInListener {
        fun onLoginClick()
        fun toggleUsernameFocusState(isFocused: Boolean)
        fun onUsernameTextChanged(text: String)
        fun toggleSignUpPressedState(isPressed: Boolean)
        fun goToSignUp()
    }
}