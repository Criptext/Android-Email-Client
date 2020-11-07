package com.criptext.mail.scenes.signin.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.CountDownTimer
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.compat.ViewAnimationUtilsCompat
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputEditText

/**
 * Created by sebas on 3/2/18.
 */

class LoginHolder(
        val view: View,
        initialState: SignInLayoutState.Login,
        firstTime: Boolean,
        isMultipleAccountLogin: Boolean): BaseSignInHolder() {

    private val rootLayout: View = view.findViewById<View>(R.id.viewRoot)
    private val usernameInput : AppCompatEditText = view.findViewById(R.id.input_username)
    private val usernameInputLayout : TextInputLayout = view.findViewById(R.id.input_username_layout)
    private val forgotPassword : TextView = view.findViewById(R.id.forgot_password)
    private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)
    private val password: TextInputEditText = view.findViewById(R.id.password)
    private val signInButton : Button = view.findViewById(R.id.signin_button)
    private val progressBar: ProgressBar = view.findViewById(R.id.signin_progress_login)
    private val imageError: ImageView = view.findViewById(R.id.signin_error_image)
    private val backButton: View = view.findViewById(R.id.icon_back)

    init {
        usernameInput.text = SpannableStringBuilder(initialState.username)
        password.text = SpannableStringBuilder(initialState.password)
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.login_password_eye))
        setListeners()

        if(firstTime) {
            rootLayout.visibility = View.INVISIBLE
            addGlobalLayout()
        }

        if(initialState.username.isNotEmpty() && initialState.password.length >= AccountDataValidator.minimumPasswordLength){
            setSubmitButtonState(ProgressButtonState.enabled)
        } else {
            setSubmitButtonState(ProgressButtonState.disabled)
        }

        if(isMultipleAccountLogin)
            backButton.visibility = View.VISIBLE
    }

    private fun addGlobalLayout(){

        val viewTreeObserver = rootLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    revealActivity(view.resources.displayMetrics.widthPixels / 2,
                            view.resources.displayMetrics.heightPixels / 2)
                    rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    private fun revealActivity(x: Int, y: Int) {

        val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
        val circularReveal = ViewAnimationUtilsCompat.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
        rootLayout.visibility = View.VISIBLE
        circularReveal?.start()

    }

    private fun setListeners() {

        signInButton.setOnClickListener {
            uiObserver?.onSubmitButtonClicked()
        }
        usernameInputLayout.setOnFocusChangeListener { _, isFocused ->
            uiObserver?.toggleUsernameFocusState(isFocused)
        }
        backButton.setOnClickListener{
            uiObserver?.onBackPressed()
        }
        forgotPassword.setOnClickListener {
            uiObserver?.onForgotPasswordClick()
        }

        usernameInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onUsernameTextChanged(text.toString())
            }
        })

        password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onPasswordChangeListener(text.toString())
            }
        })
    }

    fun resetInput() {
        usernameInput.clearComposingText()
        usernameInput.text = SpannableStringBuilder("")
        password.clearComposingText()
        password.text = SpannableStringBuilder("")
        setSubmitButtonState(ProgressButtonState.disabled)
    }

    @SuppressLint("RestrictedApi")
    fun drawError(uiMessage: UIMessage?) {
        if(uiMessage != null) {
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.error_color))
            imageError.visibility = View.VISIBLE
            usernameInput.error = view.context.getLocalizedUIMessage(uiMessage)
            signInButton.isEnabled = false
        }
    }

    fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                signInButton.visibility = View.VISIBLE
                signInButton.isEnabled = false
                progressBar.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                signInButton.visibility = View.VISIBLE
                signInButton.isEnabled = true
                progressBar.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                signInButton.visibility = View.GONE
                signInButton.isEnabled = false
                progressBar.visibility = View.VISIBLE
            }
        }
    }

}