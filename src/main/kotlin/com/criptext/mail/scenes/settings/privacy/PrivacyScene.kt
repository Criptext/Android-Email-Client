package com.criptext.mail.scenes.settings.privacy

import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.Settings2FADialog
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.ui.SyncDeviceAlertDialog
import com.criptext.mail.utils.uiobserver.UIObserver


interface PrivacyScene{

    fun attachView(uiObserver: PrivacyUIObserver,
                   keyboardManager: KeyboardManager, model: PrivacyModel, hasEncryption: Boolean)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun updateReadReceipts(isChecked: Boolean)
    fun enableReadReceiptsSwitch(isEnabled: Boolean)
    fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean)
    fun enableTwoFASwitch(isEnabled: Boolean)
    fun updateTwoFa(isChecked: Boolean)
    fun enableHasEncryptionSwitch(isEnabled: Boolean)
    fun updateHasEncryption(isChecked: Boolean)


    class Default(val view: View): PrivacyScene{
        private lateinit var privacyUIObserver: PrivacyUIObserver

        private val context = view.context

        private val twoFASwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_two_fa_switch)
        }

        private val readReceiptsSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_read_receipts_switch)
        }

        private val hasEncryptionSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_has_encryption_switch)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val confirmPasswordDialog = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val twoFADialog = Settings2FADialog(context)

        override fun attachView(uiObserver: PrivacyUIObserver, keyboardManager: KeyboardManager,
                                model: PrivacyModel, hasEncryption: Boolean) {

            privacyUIObserver = uiObserver

            backButton.setOnClickListener {
                privacyUIObserver.onBackButtonPressed()
            }

            enableReadReceiptsSwitch(true)
            updateReadReceipts(model.readReceipts)
            enableTwoFASwitch(true)
            updateTwoFa(model.twoFA)
            enableHasEncryptionSwitch(true)
            updateHasEncryption(hasEncryption)

            setSwitchListener()
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

        override fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean) {
            twoFADialog.showLogoutDialog(hasRecoveryEmailConfirmed)
        }

        override fun enableTwoFASwitch(isEnabled: Boolean) {
            twoFASwitch.isEnabled = isEnabled
        }

        override fun updateTwoFa(isChecked: Boolean) {
            twoFASwitch.setOnCheckedChangeListener { _, _ ->  }
            twoFASwitch.isChecked = isChecked
            setSwitchListener()
        }

        override fun enableReadReceiptsSwitch(isEnabled: Boolean) {
            readReceiptsSwitch.isEnabled = isEnabled
        }

        override fun updateReadReceipts(isChecked: Boolean) {
            readReceiptsSwitch.setOnCheckedChangeListener { _, _ ->  }
            readReceiptsSwitch.isChecked = isChecked
            setSwitchListener()
        }

        override fun enableHasEncryptionSwitch(isEnabled: Boolean) {
            hasEncryptionSwitch.isEnabled = isEnabled
        }

        override fun updateHasEncryption(isChecked: Boolean) {
            hasEncryptionSwitch.setOnCheckedChangeListener { _, _ ->  }
            hasEncryptionSwitch.isChecked = isChecked
            setSwitchListener()
        }

        private fun setSwitchListener(){
            readReceiptsSwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onReadReceiptsSwitched(isChecked)
            }
            twoFASwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onTwoFASwitched(isChecked)
            }
            hasEncryptionSwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onHasEncryptionSwitched(isChecked)
            }
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(privacyUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(privacyUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(privacyUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(privacyUIObserver, trustedDeviceInfo)
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