package com.criptext.mail.scenes.signup.holders

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputLayout

class SignUpConfirmPasswordHolder(
        val view: View,
        val confirmPassword: String): BaseSignUpHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val passwordInput: TextInputLayout = view.findViewById(R.id.password_input)
    private val password: AppCompatEditText = view.findViewById(R.id.password)
    private val firstCheck: ImageView = view.findViewById(R.id.password_check_1)
    private val firstX: ImageView = view.findViewById(R.id.password_x)
    private val firstCheckTextView: TextView = view.findViewById(R.id.password_check_text_1)

    init {
        password.text = SpannableStringBuilder(confirmPassword)
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(view.context, R.color.sign_up_password_eye))
        setListeners()
    }

    private fun setListeners() {
        password.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onConfirmPasswordChangedListener(text.toString())
            }
        })
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
    }

    fun checkConditions(passwordMatches: FormInputState){
        when(passwordMatches){
            is FormInputState.Valid -> {
                nextButton.isEnabled = true
                firstCheck.visibility = View.VISIBLE
                firstX.visibility = View.GONE
                firstCheckTextView.setTextColor(ContextCompat.getColor(view.context,
                        R.color.check_success_green))
            }
            is FormInputState.Error -> {
                firstCheck.visibility = View.GONE
                firstX.visibility = View.VISIBLE
                firstCheckTextView.setTextColor(ContextCompat.getColor(view.context,
                        R.color.error_color))
            }
            else -> {
                firstCheck.visibility = View.GONE
                firstX.visibility = View.GONE
                firstCheckTextView.setTextColor(view.context.getColorFromAttr(R.attr.criptextSecondaryTextColor))
            }
        }
    }

    override fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = true
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                nextButton.visibility = View.GONE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.VISIBLE
            }
        }
    }

}