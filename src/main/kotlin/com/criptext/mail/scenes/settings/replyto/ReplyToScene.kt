package com.criptext.mail.scenes.settings.replyto

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.AccountSuspendedDialog
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.ui.SyncDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver
import com.google.android.material.textfield.TextInputLayout

interface ReplyToScene{

    fun attachView(replyToUIObserver: ReplyToUIObserver, replyToEmail: String,
                   keyboardManager: KeyboardManager)
    fun showMessage(message: UIMessage)
    fun setEmailError(message: UIMessage?)
    fun enableSaveButton()
    fun disableSaveButton()
    fun clearTextBox()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, showButton: Boolean)
    fun dismissAccountSuspendedDialog()

    class Default(val view: View): ReplyToScene{

        private val context = view.context

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewStatus: TextView by lazy {
            view.findViewById<TextView>(R.id.textview_status)
        }

        private val swicthStatus: Switch by lazy {
            view.findViewById<Switch>(R.id.switch_status)
        }

        private val viewSignature: View by lazy{
            view.findViewById<View>(R.id.view_signature)
        }

        private val bodyEditText: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.input)
        }

        private val editTextEmailLayout: TextInputLayout by lazy {
            view.findViewById<TextInputLayout>(R.id.input_layout)
        }

        private val changeEmailButton: Button by lazy {
            view.findViewById<Button>(R.id.change_email_button)
        }

        var observer: ReplyToUIObserver? = null

        override fun attachView(replyToUIObserver: ReplyToUIObserver, replyToEmail: String,
                                keyboardManager: KeyboardManager) {

            observer = replyToUIObserver
            backButton.setOnClickListener {
                observer?.onBackButtonPressed()
            }

            changeEmailButton.setOnClickListener {
                observer?.onRecoveryChangeButonPressed()
            }

            textListener(observer)

            displayReplyTo(replyToEmail, keyboardManager, observer)
        }

        private fun hidePasswordError() {
            editTextEmailLayout.error = ""
        }

        private fun showPasswordError(message: UIMessage) {
            editTextEmailLayout.error = context.getLocalizedUIMessage(message)
        }

        override fun setEmailError(message: UIMessage?) {
            if (message == null) {
                hidePasswordError()
                enableSaveButton()
            } else {
                showPasswordError(message)
                disableSaveButton()
            }
        }

        override fun disableSaveButton() {
            changeEmailButton.isEnabled = false
        }

        override fun enableSaveButton() {
            changeEmailButton.isEnabled = true
        }

        private fun textListener(uiObserver: ReplyToUIObserver?) {
            bodyEditText.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    uiObserver?.onRecoveryEmailChanged(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        override fun clearTextBox() {
            bodyEditText.setText("")
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

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(observer, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(observer, trustedDeviceInfo)
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

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, showButton: Boolean) {
            accountSuspended.showDialog(observer, email, showButton)
        }

        private fun displayReplyTo(replyToEmail: String, keyboardManager: KeyboardManager,
                                   uiObserver: ReplyToUIObserver?) {
            bodyEditText.setText(replyToEmail)
            if(replyToEmail.isNotEmpty()){
                swicthStatus.isChecked = true
                textViewStatus.text = context.resources.getString(R.string.on)
                viewSignature.visibility = View.VISIBLE
            }

            swicthStatus.setOnCheckedChangeListener { _, isChecked ->
                viewSignature.visibility = if(isChecked) View.VISIBLE else View.GONE
                textViewStatus.text = if(isChecked) context.resources.getString(R.string.on)
                else context.resources.getString(R.string.off)
                if(isChecked){
                    keyboardManager.showKeyboard(bodyEditText)
                }
                else{
                    uiObserver?.onTurnOffReplyTo()
                    keyboardManager.hideKeyboard()
                }
            }
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