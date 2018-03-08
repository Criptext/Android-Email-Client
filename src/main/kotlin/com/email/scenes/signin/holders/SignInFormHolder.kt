package com.email.scenes.signin.holders

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
import com.email.scenes.signin.SignInSceneController

/**
 * Created by sebas on 3/2/18.
 */

class SignInFormHolder(val view: View) {

    private val usernameInput : AppCompatEditText
    //private val usernameInputLayout : TextInputLayout
    private val signInButton : Button
    private val signUpTextView: TextView
    private val progressBar: ProgressBar
    private val imageError: ImageView

   var signInListener: SignInSceneController.SignInListener? = null

    private val shouldButtonBeEnabled : Boolean
        get() = true

    init {
        usernameInput = view.findViewById(R.id.input_username)
        signInButton = view.findViewById(R.id.signin_button)
        //usernameInputLayout = view.findViewById(R.id.input_username_layout)
        signUpTextView = view.findViewById(R.id.signup_textview)
        progressBar = view.findViewById(R.id.signin_progress_login)
        imageError = view.findViewById(R.id.signin_error_image)
        signInButton.isEnabled  = false
        showNormalTints()
    }

    fun showNormalTints(){
        //usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
        setUsernameBackgroundTintList()
    }

    @SuppressLint("RestrictedApi")
    private fun setUsernameBackgroundTintList() {
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
    }

    @SuppressLint("RestrictedApi")
    fun drawNormalSignInOptions() {
/*        usernameInputLayout.hint = "Username"
        usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login)*/
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.signup_hint_color))
        signInButton.isEnabled = shouldButtonBeEnabled
        imageError.visibility = View.INVISIBLE
    }

    fun assignLoginButtonListener() {
        signInButton.setOnClickListener {
            // start progress dialog... change UI
            signInListener!!.onLoginClick()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun assignSignUpTextViewListener() {
        signUpTextView.setOnClickListener{
            signInListener!!.goToSignUp()
        }
    }

    fun assignUsernameInputListener(){
        //usernameInputLayout.setOnFocusChangeListener { _, isFocused ->
         //   signInListener!!.toggleUsernameFocusState(isFocused)
        //}


        usernameInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                signInListener!!.onUsernameTextChanged(text.toString())
            }

        })

    }

    fun toggleSignUpPressed(isPressed: Boolean){
        if(isPressed) {
            signUpTextView.setTextColor(
                    ContextCompat.getColor(view.context, R.color.black))
        } else {
            signUpTextView.setTextColor(
                    ContextCompat.getColor(view.context, R.color.white))
        }
    }

    @SuppressLint("RestrictedApi")
    fun drawError() {
/*        usernameInputLayout.hint = "Username does not exist"
        usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
        usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
        imageError.visibility = View.VISIBLE
        signInButton.isEnabled  = false*/
    }

    fun toggleLoginProgressBar(isLoggingIn : Boolean){
        if(isLoggingIn) {
            signInButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

}