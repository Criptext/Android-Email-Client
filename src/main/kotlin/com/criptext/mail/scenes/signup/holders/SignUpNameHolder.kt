package com.criptext.mail.scenes.signup.holders

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState

class SignUpNameHolder(
        val view: View,
        val name: String): BaseSignUpHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val fullName: AppCompatEditText = view.findViewById(R.id.full_name)
    private val fullNameInput: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.full_name_input),
            editText = fullName,
            validView = null,
            errorView = null,
            disableSubmitButton = { -> nextButton.isEnabled = false })

    init {
        setListeners()
    }

    private fun setListeners() {
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        assignFullNameTextChangeListener()
    }

    fun setState(state: FormInputState) {
        fullNameInput.setState(state)
    }

    private fun assignFullNameTextChangeListener() {
        fullName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onFullNameTextChangeListener(text.toString())
            }
        })
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