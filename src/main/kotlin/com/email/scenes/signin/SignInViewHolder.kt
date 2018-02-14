package com.email.scenes.signin

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.email.R

/**
 * Created by sebas on 2/15/18.
 */

class SignInViewHolder(val mActivity: AppCompatActivity) {
    private val res = mActivity.resources
    private val usernameInput : AppCompatEditText
    private val usernameInputLayout : TextInputLayout
    private val signInButton : Button
    private val signUpTextView: TextView
    private val progressBar: ProgressBar
    private val imageError: ImageView

    private val shouldButtonBeEnabled : Boolean
        get() = usernameInputLayout.hint == "Username" && usernameInput.text.length > 0

    fun assignLoginButtonListener(loginListener: SignInSceneController.SignInListener.LoginButtonListener) {
        signInButton.setOnClickListener {
            // start progress dialog... change UI
            loginListener.onLoginClick()
        }
    }

    fun assignUsernameInputListener(usernameInputListener: SignInSceneController.SignInListener.UsernameInputListener) {
        usernameInputLayout.setOnFocusChangeListener { view, isFocused ->
            usernameInputListener.toggleFocusState(isFocused = isFocused)
        }

        usernameInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                usernameInputListener.onTextChanged(text.toString())
            }

        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun assignSignUpTextViewListener(signUpTextViewListener: SignInSceneController.SignInListener.SignUpTextViewListener) {
        signUpTextView.setOnClickListener{
            signUpTextViewListener.goToSignUp()
        }
    }

    fun toggleSignUpPressed(isFocused: Boolean){
        if(isFocused) {
            signUpTextView.setTextColor(res.getColor(R.color.black))
        } else {
            signUpTextView.setTextColor(res.getColor(R.color.white))
        }
    }
    @SuppressLint("RestrictedApi")
    fun drawError() {
        usernameInputLayout.hint = "Username does not exist"
        usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.black))
        imageError.visibility = View.VISIBLE
        signInButton.isEnabled  = false
    }

    @SuppressLint("RestrictedApi")
    fun setUsernameBackgroundTintList() {
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.white))
    }

    @SuppressLint("RestrictedApi")
    fun drawNormalSignInOptions(){
        usernameInputLayout.hint = "Username"
        usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(res.getColor(R.color.white))
        signInButton.isEnabled = shouldButtonBeEnabled
        imageError.visibility = View.INVISIBLE
    }

    fun drawSuccess() {
        TODO("Show progress dialog...")
    }

    fun toggleLoginProgressBar(isLoginIn : Boolean){
        if(isLoginIn) {
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
        usernameInput = mActivity.findViewById(R.id.input_username)
        signInButton = mActivity.findViewById(R.id.signin_button)
        usernameInputLayout = mActivity.findViewById(R.id.input_username_layout)
        signUpTextView = mActivity.findViewById(R.id.signup_textview)
        progressBar = mActivity.findViewById(R.id.signin_progress_login)
        imageError = mActivity.findViewById(R.id.signin_error_image)
        signInButton.isEnabled  = false
        showNormalTints()
    }

    fun initListeners(signInListener: SignInSceneController.SignInListener.Default) {
        assignLoginButtonListener(signInListener.loginButtonListener)
        assignSignUpTextViewListener(signInListener.signUpTextViewListener)
        assignUsernameInputListener(signInListener.usernameInputListener)
    }
}
