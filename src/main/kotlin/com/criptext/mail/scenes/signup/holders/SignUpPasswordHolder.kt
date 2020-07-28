package com.criptext.mail.scenes.signup.holders

import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputLayout

class SignUpPasswordHolder(
        val view: View,
        passwordText: String): BaseSignUpHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val password: AppCompatEditText = view.findViewById(R.id.password)
    private val firstCheck: ImageView = view.findViewById(R.id.password_check_1)
    private val secondCheck: ImageView = view.findViewById(R.id.password_check_2)
    private val firstCheckTextView: TextView = view.findViewById(R.id.password_check_text_1)
    private val secondCheckTextView: TextView = view.findViewById(R.id.password_check_text_2)

    init {
        password.text = SpannableStringBuilder(passwordText)
        setListeners()
    }

    private fun setListeners() {
        password.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onPasswordChangedListener(text.toString())
            }
        })
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
    }

    fun checkConditions(isNotUsername: Boolean, isAtLeastEightChars: Boolean){
        nextButton.isEnabled = isNotUsername && isAtLeastEightChars
        if(isNotUsername){
            firstCheck.visibility = View.VISIBLE
            firstCheckTextView.setTextColor(ContextCompat.getColor(view.context,
                    R.color.check_success_green))
        } else {
            firstCheck.visibility = View.GONE
            firstCheckTextView.setTextColor(
                    view.context.getColorFromAttr(R.attr.criptextSecondaryTextColor)
            )
        }
        if(isAtLeastEightChars){
            secondCheck.visibility = View.VISIBLE
            secondCheckTextView.setTextColor(ContextCompat.getColor(view.context,
                    R.color.check_success_green))
        } else {
            secondCheck.visibility = View.GONE
            secondCheckTextView.setTextColor(
                    view.context.getColorFromAttr(R.attr.criptextSecondaryTextColor)
            )
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