package com.criptext.mail.scenes.signup.holders

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState

class SignUpTermsAndConditionsHolder(
        val view: View): BaseSignUpHolder() {

    private val checkboxTerms: CheckBox = view.findViewById(R.id.chkTermsAndConditions)
    private val txtTermsAndConditions: TextView = view.findViewById(R.id.txt_terms_and_conditions)
    private val creatingKeysText: TextView = view.findViewById(R.id.creatingKeysTextView)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val overlayView: View = view.findViewById(R.id.creating_account_overlay)
    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.progress_horizontal)

    init {
        setListeners()
    }

    private fun setListeners() {
        checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            uiObserver?.onCheckedOptionChanged(isChecked)
        }
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }
        nextButton.setOnClickListener {
            uiObserver?.onNextButtonPressed()
        }
        txtTermsAndConditions.setOnClickListener {
            uiObserver?.onTermsAndConditionsClick()
        }
    }

    fun showCreatingAccount(show: Boolean){
        if(show){
            overlayView.visibility = View.VISIBLE
            nextButtonProgress.visibility = View.VISIBLE
            creatingKeysText.visibility = View.VISIBLE
        } else {
            overlayView.visibility = View.GONE
            nextButtonProgress.visibility = View.GONE
            creatingKeysText.visibility = View.GONE
        }
    }

    override fun setSubmitButtonState(state : ProgressButtonState) {
        when (state) {
            ProgressButtonState.disabled -> {
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = false
            }
            ProgressButtonState.enabled -> {
                nextButton.visibility = View.VISIBLE
                nextButton.isEnabled = true
            }
            ProgressButtonState.waiting -> {
                nextButton.isEnabled = false
            }
        }
    }

}