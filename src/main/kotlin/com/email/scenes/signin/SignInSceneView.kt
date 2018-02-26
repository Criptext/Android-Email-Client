package com.email.scenes.signin

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.email.R

/**
 * Created by sebas on 2/15/18.
 */

interface SignInScene {
    fun drawNormalSignInOptions()
    fun toggleSignUpPressed(isPressed: Boolean)
    fun toggleLoginProgressBar(isLoggingIn : Boolean)
    fun drawError()
    fun drawSuccess()
    fun initListeners(signInListener: SignInSceneController.SignInListener)
    class SignInSceneView(val view: View): SignInScene {
        private val res = view.context.resources
        private val usernameInput : AppCompatEditText
        private val usernameInputLayout : TextInputLayout
        private val signInButton : Button
        private val signUpTextView: TextView
        private val progressBar: ProgressBar
        private val imageError: ImageView

        private val shouldButtonBeEnabled : Boolean
            get() = usernameInputLayout.hint == "Username" && usernameInput.text.length > 0

        private lateinit var signInListener: SignInSceneController.SignInListener
        fun assignLoginButtonListener() {
            signInButton.setOnClickListener {
                // start progress dialog... change UI
                signInListener.onLoginClick()
            }
        }

        fun assignUsernameInputListener(){
            usernameInputLayout.setOnFocusChangeListener { _, isFocused ->
                signInListener.toggleUsernameFocusState(isFocused)
            }

            usernameInput.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    signInListener.onUsernameTextChanged(text.toString())
                }

            })
        }

        @SuppressLint("ClickableViewAccessibility")
        fun assignSignUpTextViewListener() {
            signUpTextView.setOnClickListener{
                signInListener.goToSignUp()
            }
        }

        override fun toggleSignUpPressed(isPressed: Boolean){
            if(isPressed) {
                signUpTextView.setTextColor(
                        ContextCompat.getColor(view.context, R.color.black))
            } else {
                signUpTextView.setTextColor(
                        ContextCompat.getColor(view.context, R.color.white))
            }
        }
        @SuppressLint("RestrictedApi")
        override fun drawError() {
            usernameInputLayout.hint = "Username does not exist"
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.black))
            imageError.visibility = View.VISIBLE
            signInButton.isEnabled  = false
        }

        @SuppressLint("RestrictedApi")
        private fun setUsernameBackgroundTintList() {
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.white))
        }

        @SuppressLint("RestrictedApi")
        override fun drawNormalSignInOptions(){
            usernameInputLayout.hint = "Username"
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.white))
            signInButton.isEnabled = shouldButtonBeEnabled
            imageError.visibility = View.INVISIBLE
        }

        override fun drawSuccess() {
            TODO("Show progress dialog...")
        }

        override fun toggleLoginProgressBar(isLoggingIn : Boolean){
            if(isLoggingIn) {
                signInButton.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            } else {
                signInButton.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        private fun showNormalTints(){
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
            setUsernameBackgroundTintList()
        }
        init {
            usernameInput = view.findViewById(R.id.input_username)
            signInButton = view.findViewById(R.id.signin_button)
            usernameInputLayout = view.findViewById(R.id.input_username_layout)
            signUpTextView = view.findViewById(R.id.signup_textview)
            progressBar = view.findViewById(R.id.signin_progress_login)
            imageError = view.findViewById(R.id.signin_error_image)
            signInButton.isEnabled  = false
            showNormalTints()
        }

        override fun initListeners(
                signInListener: SignInSceneController.SignInListener
        ) {
            this.signInListener = signInListener
            assignLoginButtonListener()
            assignSignUpTextViewListener()
            assignUsernameInputListener()
        }
    }
}

