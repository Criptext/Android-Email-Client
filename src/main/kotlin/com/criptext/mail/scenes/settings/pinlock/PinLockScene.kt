package com.criptext.mail.scenes.settings.pinlock

import android.view.View
import android.widget.Switch
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver


interface PinLockScene{

    fun attachView(uiObserver: PinLockUIObserver,
                   keyboardManager: KeyboardManager, model: PinLockModel)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun setPinLockStatus(isEnabled: Boolean)

    class Default(val view: View): PinLockScene{
        private lateinit var pinLockUIObserver: PinLockUIObserver

        private val context = view.context

        private val pinEnableSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.pin_lock_switch)
        }

        private val changePinButton: View by lazy {
            view.findViewById<View>(R.id.change_pin)
        }

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)

        override fun attachView(uiObserver: PinLockUIObserver, keyboardManager: KeyboardManager,
                                model: PinLockModel) {

            pinLockUIObserver = uiObserver

            pinEnableSwitch.setOnCheckedChangeListener {_, isChecked ->
                pinLockUIObserver.onPinSwitchChanged(isChecked)
            }

            changePinButton.setOnClickListener {
                pinLockUIObserver.onPinChangePressed()
            }
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPasswordDialog.showDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPasswordDialog.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPasswordDialog.setPasswordError(message)
        }

        override fun showForgotPasswordDialog(email: String) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun setPinLockStatus(isEnabled: Boolean) {
            pinEnableSwitch.setOnCheckedChangeListener { _, _ ->  }
            pinEnableSwitch.isChecked = isEnabled
            pinEnableSwitch.setOnCheckedChangeListener {_, isChecked ->
                pinLockUIObserver.onPinSwitchChanged(isChecked)
            }
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(pinLockUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(pinLockUIObserver, untrustedDeviceInfo)
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