package com.criptext.mail.scenes.signin.holders

import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.validation.ProgressButtonState

/**
 * Created by sebas on 3/8/18.
 */

class PasswordLoginHolder(
        val view: View,
        initialState: SignInLayoutState.InputPassword
): BaseSignInHolder() {

    private val title: TextView = view.findViewById(R.id.textViewTitle)
    private val username: TextView = view.findViewById(R.id.username)
    private val forgotPassword : TextView = view.findViewById(R.id.forgot_password)
    private val password: TextInputEditText = view.findViewById(R.id.password)
    private val buttonConfirm: Button = view.findViewById(R.id.buttonConfirm)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val progressBar: View = view.findViewById(R.id.signin_progress_login)

    init {
        if(!initialState.hasTwoFA){
            title.text = view.context.getString(R.string.login)
            buttonConfirm.text = view.context.getString(R.string.button_confirm)
        }else{
            title.text = view.context.getString(R.string.two_fa)
            buttonConfirm.text = view.context.getString(R.string.button_next)
        }

        username.text  = "${initialState.username}@${Contact.mainDomain}"
        password.text = SpannableStringBuilder(initialState.password)
        setListeners()
        setSubmitButtonState(initialState.buttonState)
    }


    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }

        forgotPassword.setOnClickListener {
            uiObserver?.onForgotPasswordClick()
        }

        buttonConfirm.setOnClickListener {
            uiObserver?.onSubmitButtonClicked()
        }

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

    fun setSubmitButtonState(state : ProgressButtonState){
        when (state) {
            ProgressButtonState.disabled -> {
                buttonConfirm.visibility = View.VISIBLE
                buttonConfirm.isEnabled = false
                progressBar.visibility = View.INVISIBLE
            }
            ProgressButtonState.enabled -> {
                buttonConfirm.visibility = View.VISIBLE
                buttonConfirm.isEnabled = true
                progressBar.visibility = View.INVISIBLE
            }
            ProgressButtonState.waiting -> {
                buttonConfirm.visibility = View.INVISIBLE
                buttonConfirm.isEnabled = false
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    fun toggleForgotPasswordClickable(isEnable: Boolean){
        forgotPassword.isEnabled = isEnable
    }

    fun resetInput() {
        password.clearComposingText()
        password.text = SpannableStringBuilder("")
        setSubmitButtonState(ProgressButtonState.disabled)
    }
}
