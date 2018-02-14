package com.email.scenes.signin

import android.content.Intent
import com.email.SignUpActivity
import com.email.scenes.SceneController

/**
 * Created by sebas on 2/15/18.
 */

class SignInSceneController(
        val model: SignInSceneModel,
        val holder: SignInViewHolder,
        val dataSource: SignInDataSource): SceneController() {

    override val menuResourceId: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    private var loginButtonListener = object : SignInSceneController.SignInListener.LoginButtonListener {
        override fun onLoginClick() {
            validateUsername(model.username)
        }
    }

    private var usernameInputListener = object : SignInSceneController.SignInListener.UsernameInputListener{
        override fun toggleFocusState(isFocused: Boolean) {
            if(isFocused) {
            } else {
            }
        }

        override fun onTextChanged(text: String) {
            holder.drawNormalSignInOptions()
            model.username = text
        }

    }

    private var signupListener = object : SignInSceneController.SignInListener.SignUpTextViewListener{
        override fun goToSignUp() {
            val intent = Intent(holder.mActivity, SignUpActivity::class.java)
            holder.mActivity.startActivity(intent)
        }

        override fun togglePressedState(isPressed: Boolean) {
            holder.toggleSignUpPressed(isPressed)
        }
    }

    private val signInListener = SignInListener.Default(
            signUpTextViewListener = signupListener,
            usernameInputListener = usernameInputListener,
            loginButtonListener = loginButtonListener)
    private var progressSignInListener = object : ProgressSignInListener{
        override fun onStart() {
            holder.toggleLoginProgressBar(isLoginIn = true)
        }

        override fun onFinish() {
            holder.toggleLoginProgressBar(isLoginIn = false)
        }

    }

    fun validateUsername(username: String) {
        if(username == "sebas") {
            holder.drawError()
        } else {
            TODO("SIGN IN, START PROGRESS DIALOG.")
        }
    }

    override fun onStart() {
        holder.initListeners(signInListener = signInListener)
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    interface SignInListener {
        interface LoginButtonListener {
            fun onLoginClick()
        }

        interface UsernameInputListener {
            fun toggleFocusState(isFocused: Boolean)
            fun onTextChanged(text: String)
        }

        interface SignUpTextViewListener {
            fun togglePressedState(isPressed: Boolean)
            fun goToSignUp()
        }

        class Default(val signUpTextViewListener: SignUpTextViewListener,
                    val usernameInputListener: UsernameInputListener,
                      val loginButtonListener: LoginButtonListener): SignInListener
    }
}