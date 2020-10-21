package com.criptext.mail.scenes.signup.holders

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState

class SignUpRecoveryEmailHolder(
        val view: View,
        val recoveryEmailString: String): BaseSignUpHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val recoveryEmail: AppCompatEditText = view.findViewById(R.id.recovery_email)
    private val recoveryEmailInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.recovery_email_input),
            editText = recoveryEmail,
            validView = null,
            errorView = null,
            disableSubmitButton = { -> nextButton.isEnabled = false })

    init {
        recoveryEmail.text = SpannableStringBuilder(recoveryEmailString)
        setListeners()
    }

    private fun setListeners() {
        backButton.setOnClickListener{
            uiObserver?.onBackPressed()
        }
        nextButton.setOnClickListener{
            uiObserver?.onNextButtonPressed()
        }

        recoveryEmail.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onRecoveryEmailTextChangeListener(text.toString())
            }
        })
    }

    fun setState(state: FormInputState) {
        recoveryEmailInput.setState(state)
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