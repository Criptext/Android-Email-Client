package com.criptext.mail.scenes.settings.recovery_email

import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.validation.FormInputState


interface RecoveryEmailScene{

    fun attachView(recoveryEmailUIObserver: RecoveryEmailUIObserver,
                   keyboardManager: KeyboardManager, model: RecoveryEmailModel)
    fun showMessage(message: UIMessage)
    fun onResendLinkTimeSet(startTime: Long)
    fun onResendLinkFailed()
    fun showConfirmationSentDialog()
    fun showEnterPasswordDialog()
    fun enableChangeButton()
    fun disableChangeButton()
    fun dialogToggleLoad(loading: Boolean)
    fun enterPasswordDialogError(message: UIMessage)
    fun enterPasswordDialogDismiss()
    fun setRecoveryEmailState(state: FormInputState)
    fun updateCurrent(model: RecoveryEmailModel)
    fun showForgotPasswordDialog(email: String)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)

    class Default(val view: View): RecoveryEmailScene{
        private lateinit var recoveryEmailUIObserver: RecoveryEmailUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewCurrentEmail: TextView by lazy {
            view.findViewById<TextView>(R.id.recovery_email_text)
        }

        private val textViewConfirmText: TextView by lazy {
            view.findViewById<TextView>(R.id.not_confirmed_text)
        }

        private val resendLinkProgressButton: View by lazy {
            view.findViewById<View>(R.id.resend_progress_button)
        }

        private val resendLinkButton: Button by lazy {
            view.findViewById<Button>(R.id.resend_link_button)
        }

        private val changeEmailButton: Button by lazy {
            view.findViewById<Button>(R.id.change_email_button)
        }

        private val textViewNewEmail: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.input)
        }

        private val textViewNewEmailInput: FormInputViewHolder by lazy {
            FormInputViewHolder(
                    textInputLayout = view.findViewById(R.id.input_layout),
                    editText = textViewNewEmail,
                    validView = null,
                    errorView = null,
                    disableSubmitButton = { -> changeEmailButton.isEnabled = false })
        }

        private val confirmationSentDialog = ConfirmationSentDialog(context)
        private val enterPasswordDialog = EnterPasswordDialog(context)
        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)

        override fun attachView(recoveryEmailUIObserver: RecoveryEmailUIObserver, keyboardManager: KeyboardManager,
                                model: RecoveryEmailModel) {
            this.recoveryEmailUIObserver = recoveryEmailUIObserver

            backButton.setOnClickListener {
                this.recoveryEmailUIObserver.onBackButtonPressed()
            }

            resendLinkButton.setOnClickListener {
                this.recoveryEmailUIObserver.onResendRecoveryLinkPressed()
                resendLinkButton.visibility = View.GONE
                resendLinkProgressButton.visibility = View.VISIBLE
            }

            changeEmailButton.setOnClickListener {
                this.recoveryEmailUIObserver.onChangeButtonPressed(textViewCurrentEmail.text.toString())
            }

            updateCurrentEmailStatus(model)

            textViewCurrentEmail.text = model.recoveryEmail

            recoveryEmailTextListener()
        }

        private fun updateCurrentEmailStatus(model: RecoveryEmailModel){
            if(model.isEmailConfirmed) {
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.green))
                textViewConfirmText.setText(R.string.status_confirmed)
                resendLinkButton.visibility = View.GONE
            }else{
                textViewConfirmText.setTextColor(ContextCompat.getColor(
                        view.context, R.color.red))
                textViewConfirmText.setText(R.string.status_not_confirmed)
                val lastResend = System.currentTimeMillis() - model.lastTimeConfirmationLinkSent
                if(model.lastTimeConfirmationLinkSent != 0L && (lastResend < RecoveryEmailController.RESEND_TIME)){
                    resendLinkButton.isEnabled = false
                    timerListener(RecoveryEmailController.RESEND_TIME - lastResend)
                }else {
                    if(model.recoveryEmail.isNotEmpty())
                        resendLinkButton.visibility = View.VISIBLE
                }
            }
        }

        private fun recoveryEmailTextListener() {
            textViewNewEmail.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    recoveryEmailUIObserver.onRecoveryEmailTextChanged(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        private fun timerListener(startTime: Long) {
            object : CountDownTimer(startTime, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    val min = ((millisUntilFinished / 1000) / 60).toInt()
                    val sec = ((millisUntilFinished / 1000) % 60).toInt()
                    resendLinkButton.text = if(sec < 10) "$min:0$sec" else "$min:$sec"
                }

                override fun onFinish() {
                    resendLinkButton.setText(R.string.button_resend_confirmation)
                    resendLinkButton.isEnabled = true
                }
            }.start()
        }

        override fun setRecoveryEmailState(state: FormInputState) {
            textViewNewEmailInput.setState(state)
        }

        override fun showEnterPasswordDialog() {
            enterPasswordDialog.showDialog(recoveryEmailUIObserver)
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(recoveryEmailUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(recoveryEmailUIObserver, untrustedDeviceInfo)
        }

        override fun disableChangeButton() {
            changeEmailButton.isEnabled = false
        }

        override fun enableChangeButton() {
            changeEmailButton.isEnabled = true
        }

        override fun dialogToggleLoad(loading: Boolean) {
            enterPasswordDialog.toggleLoad(loading)
        }

        override fun enterPasswordDialogError(message: UIMessage) {
            enterPasswordDialog.setPasswordError(message)
        }

        override fun enterPasswordDialogDismiss() {
            enterPasswordDialog.dismissDialog()
        }

        override fun onResendLinkTimeSet(startTime: Long) {
            timerListener(startTime)
        }

        override fun onResendLinkFailed() {
            resendLinkButton.visibility = View.VISIBLE
            resendLinkProgressButton.visibility = View.GONE
        }

        override fun showConfirmationSentDialog() {
            confirmationSentDialog.showDialog(recoveryEmailUIObserver)
            resendLinkButton.visibility = View.VISIBLE
            resendLinkButton.isEnabled = false
            resendLinkProgressButton.visibility = View.GONE
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
        }

        override fun updateCurrent(model: RecoveryEmailModel) {
            textViewCurrentEmail.text = model.recoveryEmail
            updateCurrentEmailStatus(model)
        }

        override fun showForgotPasswordDialog(email: String) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}