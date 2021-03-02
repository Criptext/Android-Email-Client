package com.criptext.mail.scenes.signin.holders

import com.google.android.material.textfield.TextInputEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordLoginHolder(
        val view: View,
        initialState: SignInLayoutState.ChangePassword
): BaseSignInHolder() {

    private val username: TextView = view.findViewById(R.id.username)
    private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)
    private val password: TextInputEditText = view.findViewById(R.id.password)
    private val confirmPasswordInput: TextInputLayout = view.findViewById(R.id.confirm_password_input)
    private val confirmPassword: TextInputEditText = view.findViewById(R.id.confirm_password)
    private val buttonConfirm: Button = view.findViewById(R.id.buttonConfirm)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val progressBar: View = view.findViewById(R.id.signin_progress_login)

    init {
        val (recipientId, domain) = if (AccountDataValidator.validateEmailAddress(initialState.username) is FormData.Valid) {
            val nonCriptextDomain = EmailAddressUtils.extractEmailAddressDomain(initialState.username)
            Pair(EmailAddressUtils.extractRecipientIdFromAddress(initialState.username, nonCriptextDomain),
                    nonCriptextDomain
            )
        } else {
            Pair(initialState.username, Contact.mainDomain)
        }
        username.text  = "$recipientId@$domain"
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.non_criptext_email_send_eye))
        confirmPasswordInput.isPasswordVisibilityToggleEnabled = true
        confirmPasswordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.non_criptext_email_send_eye))
        setListeners()
        setSubmitButtonState(initialState.buttonState)
    }


    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
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

        confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onConfirmPasswordChangeListener(text.toString())
            }
        })
    }

    fun showPasswordDialogError(message: UIMessage?){
        if (message == null){
            confirmPasswordInput.error = ""
        }
        else {
            confirmPasswordInput.error = view.context.getLocalizedUIMessage(message)
            buttonConfirm.isEnabled = false
        }
    }

    fun toggleChangePasswordButton(enable: Boolean) {
        buttonConfirm.isEnabled = enable
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
}
