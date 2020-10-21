package com.criptext.mail.scenes.signup.customize.holder

import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.validation.ProgressButtonState

class CustomizeContactsHolder(
        val view: View,
        hasAllowedContacts: Boolean): BaseCustomizeHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val skipButton: TextView = view.findViewById(R.id.skip)
    private val backButton: ImageView = view.findViewById(R.id.icon_back)
    private val contactsSwitch: Switch = view.findViewById(R.id.contacts_switch)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val awesomeTextView: TextView = view.findViewById(R.id.awesome_text)

    init {
        contactsSwitch.isChecked = hasAllowedContacts
        setListeners()
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
        contactsSwitch.setOnCheckedChangeListener { _, isChecked ->
            uiObserver?.onContactsSwitched(isChecked)
        }
    }

    fun showAwesomeText(show: Boolean){
        if(show) {
            awesomeTextView.visibility = View.VISIBLE
        } else {
            awesomeTextView.visibility = View.INVISIBLE
        }
    }

    fun updateContactSwitch(isChecked: Boolean){
        contactsSwitch.setOnCheckedChangeListener { _, _ ->  }
        contactsSwitch.isChecked = isChecked
        setListeners()
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