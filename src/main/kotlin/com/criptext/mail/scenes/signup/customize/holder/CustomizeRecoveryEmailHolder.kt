package com.criptext.mail.scenes.signup.customize.holder

import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.ProgressButtonState

class CustomizeRecoveryEmailHolder(
        val view: View,
        val recoveryEmail: String): BaseCustomizeHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val skipButton: TextView = view.findViewById(R.id.skip)
    private val backButton: ImageView = view.findViewById(R.id.icon_back)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val verifiedTextView: TextView = view.findViewById(R.id.verified_text)
    private val recoveryEmailTextView: TextView = view.findViewById(R.id.textViewEmail)

    init {
        recoveryEmailTextView.text = SpannableStringBuilder(recoveryEmail)
        setListeners()
        nextButton.isEnabled = false
        timerListener(20000L)
    }

    fun setupRecoveryEmailTimer(){
        nextButton.isEnabled = false
        timerListener(20000L)
    }


    private fun timerListener(startTime: Long) {
        object : CountDownTimer(startTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if(nextButton.isEnabled) this.cancel()
                val min = ((millisUntilFinished / 1000) / 60).toInt()
                val sec = ((millisUntilFinished / 1000) % 60).toInt()
                nextButton.text = view.context.getLocalizedUIMessage(UIMessage(R.string.button_resend_confirmation))
                        .plus(if(sec < 10) " ($min:0$sec)" else " ($min:$sec)")
            }

            override fun onFinish() {
                nextButton.setText(R.string.button_resend_confirmation)
                nextButton.isEnabled = true
            }
        }.start()
    }

    private fun setListeners() {
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
        skipButton.setOnClickListener {
            uiObserver?.onSkipButtonPressed()
        }
        backButton.setOnClickListener {
            uiObserver?.onBackButtonPressed()
        }
    }

    fun updateRecoveryEmailVerification(isVerified: Boolean){
        if(isVerified) verifiedTextView.visibility = View.VISIBLE
    }

    fun changeNextButton(){
        nextButton.setText(R.string.btn_next)
        skipButton.visibility = View.GONE
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