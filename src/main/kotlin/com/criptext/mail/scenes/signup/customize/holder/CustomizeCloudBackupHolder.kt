package com.criptext.mail.scenes.signup.customize.holder

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.validation.ProgressButtonState

class CustomizeCloudBackupHolder(
        val view: View): BaseCustomizeHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val backButton: ImageView = view.findViewById(R.id.icon_back)
    private val skipButton: TextView = view.findViewById(R.id.skip)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)

    init {
        setListeners()
    }

    private fun setListeners() {
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
        backButton.setOnClickListener {
            uiObserver?.onBackButtonPressed()
        }
        skipButton.setOnClickListener {
            uiObserver?.onSkipButtonPressed()
        }
    }

    override fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                skipButton.isEnabled = true
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.enabled -> {
                skipButton.isEnabled = true
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = true
                nextButtonProgress.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                skipButton.isEnabled = false
                nextButton.visibility = View.GONE
                nextButton.isEnabled = false
                nextButtonProgress.visibility = View.VISIBLE
            }
        }
    }

}