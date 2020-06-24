package com.criptext.mail.scenes.settings.recovery_email

import android.graphics.Typeface
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogType
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
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun loadChangeEmailButton(isLoading: Boolean)

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

        private val changeEmailButtonLoading: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.change_progress_button)
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
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

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

            textViewCurrentEmail.text = model.userData.recoveryEmail

            recoveryEmailTextListener()
        }

        private fun updateCurrentEmailStatus(model: RecoveryEmailModel){
            when {
                model.userData.isEmailConfirmed -> {
                    textViewConfirmText.setTextColor(ContextCompat.getColor(
                            view.context, R.color.green))
                    textViewConfirmText.setText(R.string.status_confirmed)
                    resendLinkButton.visibility = View.GONE
                }
                model.userData.recoveryEmail.isEmpty() -> {
                    textViewCurrentEmail.text = view.context.getLocalizedUIMessage(UIMessage(R.string.forgot_password_title_no_recovery))
                    textViewCurrentEmail.setTypeface(textViewCurrentEmail.typeface, Typeface.ITALIC)
                    resendLinkButton.visibility = View.GONE
                    textViewConfirmText.visibility = View.GONE
                }
                else -> {
                    textViewConfirmText.setTextColor(ContextCompat.getColor(
                            view.context, R.color.red))
                    textViewConfirmText.setText(R.string.status_not_confirmed)
                    val lastResend = System.currentTimeMillis() - model.lastTimeConfirmationLinkSent
                    if(model.lastTimeConfirmationLinkSent != 0L && (lastResend < RecoveryEmailController.RESEND_TIME)){
                        resendLinkButton.isEnabled = false
                        timerListener(RecoveryEmailController.RESEND_TIME - lastResend)
                    }else {
                        if(model.userData.recoveryEmail.isNotEmpty())
                            resendLinkButton.visibility = View.VISIBLE
                    }
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
                    resendLinkButton.isEnabled = false
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

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(recoveryEmailUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(recoveryEmailUIObserver, trustedDeviceInfo)
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }


        override fun disableChangeButton() {
            changeEmailButton.isEnabled = false
        }

        override fun enableChangeButton() {
            changeEmailButton.isEnabled = true
        }

        override fun loadChangeEmailButton(isLoading: Boolean) {
            if(isLoading){
                changeEmailButtonLoading.visibility = View.VISIBLE
                changeEmailButton.visibility = View.GONE
            } else {
                changeEmailButtonLoading.visibility = View.GONE
                changeEmailButton.visibility = View.VISIBLE
            }
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
            confirmationSentDialog.showDialog()
            resendLinkButton.visibility = View.VISIBLE
            resendLinkButton.isEnabled = false
            resendLinkProgressButton.visibility = View.GONE
        }

        override fun updateCurrent(model: RecoveryEmailModel) {
            textViewCurrentEmail.text = model.userData.recoveryEmail
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