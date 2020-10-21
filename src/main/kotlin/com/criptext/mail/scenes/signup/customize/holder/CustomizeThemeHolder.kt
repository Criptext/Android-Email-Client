package com.criptext.mail.scenes.signup.customize.holder

import android.R.attr.button
import android.view.View
import android.widget.*
import android.widget.RelativeLayout
import com.criptext.mail.R
import com.criptext.mail.validation.ProgressButtonState


class CustomizeThemeHolder(
        val view: View,
        private val hasDarkTheme: Boolean): BaseCustomizeHolder() {

    private val nextButton: Button = view.findViewById(R.id.next_button)
    private val backButton: ImageView = view.findViewById(R.id.icon_back)
    private val skipButton: TextView = view.findViewById(R.id.skip)
    private val nextButtonProgress: ProgressBar = view.findViewById(R.id.next_button_progress)
    private val themeSwitch: RelativeLayout = view.findViewById(R.id.theme_switch)
    private val themeSwitchButton: LinearLayout = view.findViewById(R.id.switch_button)

    init {
        setSwitch(hasDarkTheme)
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
        themeSwitch.setOnClickListener {
            uiObserver?.onThemeSwitched()
            setSwitch(hasDarkTheme)
        }
    }

    private fun setSwitch(isChecked: Boolean){
        if(isChecked){
            val params = themeSwitchButton.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            themeSwitchButton.layoutParams = params
        } else {
            val params = themeSwitchButton.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
            themeSwitchButton.layoutParams = params
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