package com.criptext.mail.scenes.settings.labels

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.SettingsCustomLabelDialog
import com.criptext.mail.scenes.settings.labels.data.LabelWrapperAdapter
import com.criptext.mail.scenes.settings.labels.data.VirtualLabelWrapperList
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView


interface LabelsScene{

    fun attachView(labelsUIObserver: LabelsUIObserver, model: LabelsModel)
    fun showMessage(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun getLabelLocalizedName(name: String): String
    fun getLabelListView(): VirtualListView
    fun showCreateLabelDialog(keyboardManager: KeyboardManager)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()

    class Default(val view: View): LabelsScene{
        private lateinit var labelsUIObserver: LabelsUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val recyclerViewLabels: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewLabels)
        }
        private val labelListView: VirtualListView = VirtualRecyclerView(recyclerViewLabels)


        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val settingCustomLabelDialog = SettingsCustomLabelDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(labelsUIObserver: LabelsUIObserver,
                                model: LabelsModel) {
            this.labelsUIObserver = labelsUIObserver

            backButton.setOnClickListener {
                this.labelsUIObserver.onBackButtonPressed()
            }

            labelListView.setAdapter(LabelWrapperAdapter(view.context, labelsUIObserver, VirtualLabelWrapperList(model)))

        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(labelsUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(labelsUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(labelsUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(labelsUIObserver, trustedDeviceInfo)
        }

        override fun dismissLinkDeviceDialog() {
            linkAuthDialog.dismiss()
        }

        override fun dismissSyncDeviceDialog() {
            syncAuthDialog.dismiss()
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

        override fun showForgotPasswordDialog(email: String) {
            ForgotPasswordDialog(context, email).showForgotPasswordDialog()
        }

        override fun getLabelListView(): VirtualListView {
            return labelListView
        }

        override fun showCreateLabelDialog(keyboardManager: KeyboardManager) {
            settingCustomLabelDialog.showCustomLabelDialog(labelsUIObserver, keyboardManager)
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun getLabelLocalizedName(name: String): String {
            return context.getLocalizedUIMessage(
                    UIUtils.getLocalizedSystemLabelName(name)
            )
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