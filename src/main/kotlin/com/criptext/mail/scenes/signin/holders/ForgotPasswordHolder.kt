package com.criptext.mail.scenes.signin.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.CountDownTimer
import com.google.android.material.textfield.TextInputEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordHolder(
        val view: View
): BaseSignInHolder() {

    private val usernameInput : AppCompatEditText = view.findViewById(R.id.input_username)
    private val usernameInputLayout : TextInputLayout = view.findViewById(R.id.input_username_layout)
    private val buttonConfirm: Button = view.findViewById(R.id.buttonConfirm)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val progressBar: View = view.findViewById(R.id.signin_progress_login)

    init {
        setListeners()
    }


    private fun setListeners() {
        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }

        buttonConfirm.setOnClickListener {
            uiObserver?.onSubmitButtonClicked()
        }

        usernameInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onUsernameTextChanged(text.toString())
            }
        })
    }

    @SuppressLint("RestrictedApi")
    fun drawError(uiMessage: UIMessage?) {
        if(uiMessage != null) {
            usernameInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
            usernameInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.error_color))
            usernameInput.error = view.context.getLocalizedUIMessage(uiMessage)
            buttonConfirm.isEnabled = false
        }
    }

    fun toggleForgotPasswordClickable(isEnable: Boolean){
        if(isEnable){
            timerListener(buttonConfirm)
        } else {
            buttonConfirm.isEnabled = isEnable
        }
    }

    private fun timerListener(textView: TextView, startTime: Long = 10000) {
        object : CountDownTimer(startTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val sec = ((millisUntilFinished / 1000) % 60).toInt()
                textView.text = view.context.getString(R.string.login_reset_password_button_time, sec)
            }

            override fun onFinish() {
                textView.text = view.context.getString(R.string.login_reset_password_button)
                textView.isEnabled = true
            }
        }.start()
    }

    fun setSubmitButtonState(state : ProgressButtonState){
        when (state) {
            ProgressButtonState.disabled -> {
                buttonConfirm.visibility = View.VISIBLE
                buttonConfirm.isEnabled = false
                progressBar.visibility = View.INVISIBLE
            }
            ProgressButtonState.enabled -> {
                buttonConfirm.visibility = View.VISIBLE
                buttonConfirm.isEnabled = true
                progressBar.visibility = View.INVISIBLE
            }
            ProgressButtonState.waiting -> {
                buttonConfirm.visibility = View.INVISIBLE
                buttonConfirm.isEnabled = false
                progressBar.visibility = View.VISIBLE
            }
        }
    }
}
