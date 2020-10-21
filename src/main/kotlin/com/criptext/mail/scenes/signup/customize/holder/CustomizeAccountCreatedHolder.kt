package com.criptext.mail.scenes.signup.customize.holder

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.signup.holders.BaseSignUpHolder
import com.criptext.mail.validation.ProgressButtonState

class CustomizeAccountCreatedHolder(
        val view: View,
        val name: String,
        val email: String): BaseCustomizeHolder() {

    private val nameTextView: TextView = view.findViewById(R.id.textViewName)
    private val emailTextView: TextView = view.findViewById(R.id.textViewEmail)
    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)

    init {
        nameTextView.text = SpannableStringBuilder(name)
        emailTextView.text = SpannableStringBuilder(email)
        setListeners()
    }

    private fun setListeners() {
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
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