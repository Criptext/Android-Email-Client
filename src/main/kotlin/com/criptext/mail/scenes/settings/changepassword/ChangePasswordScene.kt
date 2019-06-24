package com.criptext.mail.scenes.settings.changepassword

import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.uiobserver.UIObserver


interface ChangePasswordScene{

    fun attachView(uiObserver: ChangePasswordUIObserver,
                   keyboardManager: KeyboardManager, model: ChangePasswordModel)
    fun showMessage(message: UIMessage)
    fun showPasswordDialogError(message: UIMessage?)
    fun toggleChangePasswordButton(enable: Boolean)
    fun showOldPasswordError(message: UIMessage?)
    fun showForgotPasswordDialog(email: String?)
    fun dismissConfirmPasswordDialog()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, showButton: Boolean)
    fun dismissAccountSuspendedDialog()

    class Default(val view: View): ChangePasswordScene{
        private lateinit var changePasswordUIObserver: ChangePasswordUIObserver

        private val context = view.context

        private val saveButton: Button by lazy {
            view.findViewById<Button>(R.id.save_button)
        }

        private val oldPassword: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.old_password)
        }

        private val oldPasswordInput: TextInputLayout by lazy {
            view.findViewById<TextInputLayout>(R.id.password_old_input)
        }

        private val passwordInput: TextInputLayout by lazy {
            view.findViewById<TextInputLayout>(R.id.password_input)
        }

        private val password: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.password)
        }

        private val forgotPassword: TextView by lazy {
            view.findViewById<TextView>(R.id.forgot_password)
        }

        private val confirmPassword: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.password_repeat)
        }

        private val confirmPasswordInput: TextInputLayout by lazy {
            view.findViewById<TextInputLayout>(R.id.password_repeat_input)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(uiObserver: ChangePasswordUIObserver, keyboardManager: KeyboardManager,
                                model: ChangePasswordModel) {

            changePasswordUIObserver = uiObserver

            setEyeOnInputLayouts()

            saveButton.setOnClickListener {
                changePasswordUIObserver.onChangePasswordButtonPressed()
            }

            backButton.setOnClickListener {
                changePasswordUIObserver.onBackButtonPressed()
            }

            forgotPassword.setOnClickListener {
                changePasswordUIObserver.onForgotPasswordPressed()
            }

            assignOldPasswordTextListener()
            assignPasswordTextListener()
            assignConfirmPasswordTextChangeListener()

        }

        private fun setEyeOnInputLayouts(){
            passwordInput.isPasswordVisibilityToggleEnabled = true
            passwordInput.setPasswordVisibilityToggleTintList(
                    AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))
            oldPasswordInput.isPasswordVisibilityToggleEnabled = true
            oldPasswordInput.setPasswordVisibilityToggleTintList(
                    AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))
            confirmPasswordInput.isPasswordVisibilityToggleEnabled = true
            confirmPasswordInput.setPasswordVisibilityToggleTintList(
                    AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))
        }

        private fun assignOldPasswordTextListener() {
            oldPassword.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    changePasswordUIObserver.onOldPasswordChangedListener(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        private fun assignPasswordTextListener() {
            password.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    changePasswordUIObserver.onPasswordChangedListener(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        private fun assignConfirmPasswordTextChangeListener() {
            confirmPassword.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    changePasswordUIObserver.onConfirmPasswordChangedListener(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        override fun showPasswordDialogError(message: UIMessage?) {
            if (message == null){
                confirmPasswordInput.error = ""
            }
            else {
                confirmPasswordInput.error = view.context.getLocalizedUIMessage(message)
                saveButton.isEnabled = false
            }
        }

        override fun showOldPasswordError(message: UIMessage?) {
            if (message == null) {
                oldPasswordInput.error = ""
                saveButton.isEnabled = true
            }
            else {
                oldPasswordInput.error = view.context.getLocalizedUIMessage(message)
                saveButton.isEnabled = false
            }
        }

        override fun toggleChangePasswordButton(enable: Boolean) {
            saveButton.isEnabled = enable
        }

        override fun showForgotPasswordDialog(email: String?) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPasswordDialog.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPasswordDialog.setPasswordError(message)
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPasswordDialog.showDialog(observer)
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(changePasswordUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(changePasswordUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(changePasswordUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(changePasswordUIObserver, trustedDeviceInfo)
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