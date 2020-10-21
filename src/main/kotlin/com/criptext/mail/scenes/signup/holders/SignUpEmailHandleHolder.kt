package com.criptext.mail.scenes.signup.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState

class SignUpEmailHandleHolder(
        val view: View,
        emailHandle: String): BaseSignUpHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val username: AppCompatEditText = view.findViewById(R.id.username)
    private val usernameInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.input_username),
            editText = username,
            validView = null,
            errorView = null,
            disableSubmitButton = { -> nextButton.isEnabled = false })


    init {
        username.text = SpannableStringBuilder(emailHandle)
        setListeners()
    }

    fun setState(state: FormInputState) {
        usernameInput.setState(state)
    }

    private fun setListeners() {
        username.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onUsernameChangedListener(text.toString())
            }
        })
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
    }

    @SuppressLint("RestrictedApi")
    fun setError(uiMessage: UIMessage) {
        username.supportBackgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(view.context, R.color.black))
        username.error = view.context.getLocalizedUIMessage(uiMessage)
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