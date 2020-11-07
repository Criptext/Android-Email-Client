package com.criptext.mail.scenes.signin.holders

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.scenes.signin.OnPasswordLoginDialogListener
import com.criptext.mail.scenes.signin.PasswordLoginDialog
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.ProgressButtonState
import com.google.android.material.textfield.TextInputLayout


/**
 * Created by sebas on 3/8/18.
 */

class LoginValidationHolder(
        val view: View,
        val initialState: SignInLayoutState.LoginValidation
): BaseSignInHolder() {

    private val backButton: View
    private val loadingOverlay: View
    private val loadingProgress: ProgressBar
    private val buttonResend: TextView
    private val recoveryAddressLabel: TextView
    private val codeInput : AppCompatEditText = view.findViewById(R.id.recovery_code)
    private val codeInputLayout : TextInputLayout = view.findViewById(R.id.recovery_code_layout)

    private val passwordLoginDialog = PasswordLoginDialog(view.context)

    init {
        buttonResend = view.findViewById(R.id.resend_code)
        backButton = view.findViewById(R.id.icon_back)
        recoveryAddressLabel = view.findViewById(R.id.textViewEmail)
        loadingOverlay = view.findViewById(R.id.creating_account_overlay)
        loadingProgress = view.findViewById(R.id.progress_horizontal)

        if(initialState.recoveryAddress != null)
            recoveryAddressLabel.text = initialState.recoveryAddress


        setListeners()
    }

    fun setEnableButtons(enable: Boolean){
        buttonResend.isEnabled = enable
        backButton.isEnabled = enable
        buttonResend.isClickable = enable
        backButton.isClickable = enable
    }

    private fun setListeners() {

        backButton.setOnClickListener {
            uiObserver?.onBackPressed()
        }

        buttonResend.setOnClickListener {
            uiObserver?.onResendDeviceLinkAuth(initialState.username, initialState.domain)
            setEnableButtons(false)
        }

        codeInput.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if ((event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                uiObserver?.onSubmitButtonClicked()
            }
            false
        })

        codeInput.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onRecoveryCodeChangeListener(text.toString())
            }
        })

    }

    fun setSubmitButtonState(state : ProgressButtonState){
        when (state) {
            ProgressButtonState.disabled,
            ProgressButtonState.enabled -> {
                loadingOverlay.visibility = View.GONE
                loadingProgress.visibility = View.GONE
            }
            ProgressButtonState.waiting -> {
                loadingOverlay.visibility = View.VISIBLE
                loadingProgress.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("RestrictedApi")
    fun drawError(uiMessage: UIMessage?) {
        if(uiMessage != null) {
            codeInputLayout.setHintTextAppearance(R.style.textinputlayout_login_error)
            codeInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.error_color))
            codeInput.error = view.context.getLocalizedUIMessage(uiMessage)
        } else {
            codeInputLayout.setHintTextAppearance(R.style.textinputlayout_login)
            codeInput.supportBackgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(view.context, R.color.azure))
            codeInput.error = null
        }
    }

    fun showPasswordLoginDialog(onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {
        passwordLoginDialog.showPasswordLoginDialog(initialState.username, initialState.domain, onPasswordLoginDialogListener)
    }
}
