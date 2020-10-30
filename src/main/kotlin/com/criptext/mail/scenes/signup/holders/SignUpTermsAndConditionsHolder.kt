package com.criptext.mail.scenes.signup.holders

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.utils.HTMLUtils
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState

class SignUpTermsAndConditionsHolder(
        val view: View): BaseSignUpHolder() {

    private val checkboxTerms: CheckBox = view.findViewById(R.id.chkTermsAndConditions)
    private val txtTermsAndConditions: TextView = view.findViewById(R.id.txt_terms_and_conditions)
    private val creatingKeysText: TextView = view.findViewById(R.id.creatingKeysTextView)
    private val backButton: View = view.findViewById(R.id.icon_back)
    private val overlayView: View = view.findViewById(R.id.creating_account_overlay)
    private val captchaWebView: WebView = view.findViewById(R.id.captcha)
    private val captchaProgressBar: ProgressBar = view.findViewById(R.id.captcha_load)
    private val refreshCaptcha: ImageView = view.findViewById(R.id.refresh_captcha)
    private val captchaText: AppCompatEditText = view.findViewById(R.id.input)
    private val captchaTextLayout: FormInputViewHolder = FormInputViewHolder(
            textInputLayout = view.findViewById(R.id.input_layout),
            editText = captchaText,
            validView = null,
            errorView = null,
            disableSubmitButton = { -> nextButton.isEnabled = false })
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
        refreshCaptcha.setOnClickListener {
            uiObserver?.onCaptchaRefresh()
        }

        captchaText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onCaptchaTextChange(text.toString())
            }
        })
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

    fun setState(state: FormInputState) {
        captchaTextLayout.setState(state)
    }

    fun captchaIsLoading(){
        captchaProgressBar.visibility = View.VISIBLE
        captchaWebView.visibility = View.GONE
    }

    fun setCaptcha(captcha: String){
        captchaProgressBar.visibility = View.GONE
        captchaWebView.visibility = View.VISIBLE
        captchaWebView.loadDataWithBaseURL(null, HTMLUtils.createCaptchaHtml(captcha), "text/html", "UTF-8", null);
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