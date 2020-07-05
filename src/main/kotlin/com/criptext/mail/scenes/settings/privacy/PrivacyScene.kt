package com.criptext.mail.scenes.settings.privacy

import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.Settings2FADialog
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver


interface PrivacyScene{

    fun attachView(uiObserver: PrivacyUIObserver,
                   keyboardManager: KeyboardManager, model: PrivacyModel)
    fun showMessage(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()

    fun showForgotPasswordDialog(email: String)
    fun updateReadReceipts(isChecked: Boolean)
    fun enableReadReceiptsSwitch(isEnabled: Boolean)
    fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean)
    fun enableTwoFASwitch(isEnabled: Boolean)
    fun updateTwoFa(isChecked: Boolean)
    fun enableBlockRemoteContentSwitch(isEnabled: Boolean)
    fun updateBlockRemoteContent(isChecked: Boolean)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()


    class Default(val view: View): PrivacyScene{
        private lateinit var privacyUIObserver: PrivacyUIObserver

        private val context = view.context

        private val twoFASwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_two_fa_switch)
        }

        private val readReceiptsSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_read_receipts_switch)
        }

        private val blockRemoteContentSwitch: Switch by lazy {
            view.findViewById<Switch>(R.id.privacy_block_remote_content_switch)
        }

        private val twoFALoading: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.two_fa_loading)
        }

        private val readReceiptsLoading: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.read_receipts_loading)
        }

        private val blockRemoteContentLoading: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.block_remote_content_loading)
        }

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val twoFADialog = Settings2FADialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(uiObserver: PrivacyUIObserver, keyboardManager: KeyboardManager,
                                model: PrivacyModel) {

            privacyUIObserver = uiObserver

            backButton.setOnClickListener {
                privacyUIObserver.onBackButtonPressed()
            }

            setSwitchListener()
        }

        override fun showForgotPasswordDialog(email: String) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun showTwoFADialog(hasRecoveryEmailConfirmed: Boolean) {
            twoFADialog.showLogoutDialog(hasRecoveryEmailConfirmed)
        }

        override fun enableTwoFASwitch(isEnabled: Boolean) {
            twoFASwitch.isEnabled = isEnabled
            if(isEnabled){
                twoFASwitch.visibility = View.VISIBLE
                twoFALoading.visibility = View.GONE
            } else {
                twoFASwitch.visibility = View.INVISIBLE
                twoFALoading.visibility = View.VISIBLE
            }
        }

        override fun updateTwoFa(isChecked: Boolean) {
            twoFASwitch.setOnCheckedChangeListener { _, _ ->  }
            twoFASwitch.isChecked = isChecked
            setSwitchListener()
        }

        override fun enableBlockRemoteContentSwitch(isEnabled: Boolean) {
            blockRemoteContentSwitch.isEnabled = isEnabled
            if(isEnabled){
                blockRemoteContentSwitch.visibility = View.VISIBLE
                blockRemoteContentLoading.visibility = View.GONE
            } else {
                blockRemoteContentSwitch.visibility = View.INVISIBLE
                blockRemoteContentLoading.visibility = View.VISIBLE
            }
        }

        override fun updateBlockRemoteContent(isChecked: Boolean) {
            blockRemoteContentSwitch.setOnCheckedChangeListener { _, _ ->  }
            blockRemoteContentSwitch.isChecked = isChecked
            setSwitchListener()
        }

        override fun enableReadReceiptsSwitch(isEnabled: Boolean) {
            readReceiptsSwitch.isEnabled = isEnabled
            if(isEnabled){
                readReceiptsSwitch.visibility = View.VISIBLE
                readReceiptsLoading.visibility = View.GONE
            } else {
                readReceiptsSwitch.visibility = View.INVISIBLE
                readReceiptsLoading.visibility = View.VISIBLE
            }
        }

        override fun updateReadReceipts(isChecked: Boolean) {
            readReceiptsSwitch.setOnCheckedChangeListener { _, _ ->  }
            readReceiptsSwitch.isChecked = isChecked
            setSwitchListener()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }


        private fun setSwitchListener(){
            readReceiptsSwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onReadReceiptsSwitched(isChecked)
            }
            twoFASwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onTwoFASwitched(isChecked)
            }
            blockRemoteContentSwitch.setOnCheckedChangeListener { _, isChecked ->
                privacyUIObserver.onBlockRemoteContentSwitched(isChecked)
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

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
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