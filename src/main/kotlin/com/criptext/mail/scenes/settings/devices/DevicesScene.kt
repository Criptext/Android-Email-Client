package com.criptext.mail.scenes.settings.devices

import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.SettingsRemoveDeviceDialog
import com.criptext.mail.scenes.settings.devices.data.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.settings.devices.data.VirtualDeviceList
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView


interface DevicesScene{

    fun attachView(devicesUIObserver: DevicesUIObserver, keyboardManager: KeyboardManager,
                   model: DevicesModel, devicesListItemListener: DevicesListItemListener)
    fun showMessage(message: UIMessage)
    fun showForgotPasswordDialog(email: String)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun dismissConfirmPasswordDialog()
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun dismissLinkDeviceDialog()
    fun dismissSyncDeviceDialog()
    fun getDeviceListView(): VirtualListView
    fun showRemoveDeviceDialog(deviceId: Int, position: Int)
    fun removeDeviceDialogToggleLoad(loading: Boolean)
    fun setRemoveDeviceError(message: UIMessage)
    fun removeDeviceDialogDismiss()
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun dismissAccountSuspendedDialog()
    fun showProgressBar(show: Boolean)

    class Default(val view: View): DevicesScene{
        private lateinit var devicesUIObserver: DevicesUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val recyclerViewDevices: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewDevices)
        }

        private val deviceLoadProgress: ProgressBar by lazy {
            view.findViewById<ProgressBar>(R.id.deviceLoadProgress)
        }
        private val deviceListView: VirtualListView = VirtualRecyclerView(recyclerViewDevices)

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncAuthDialog = SyncDeviceAlertDialog(context)
        private val settingRemoveDeviceDialog = SettingsRemoveDeviceDialog(context)
        private val accountSuspended = AccountSuspendedDialog(context)

        override fun attachView(devicesUIObserver: DevicesUIObserver, keyboardManager: KeyboardManager,
                                model: DevicesModel, devicesListItemListener: DevicesListItemListener) {
            this.devicesUIObserver = devicesUIObserver

            backButton.setOnClickListener {
                this.devicesUIObserver.onBackButtonPressed()
            }

            deviceListView.setAdapter(DeviceAdapter(view.context, devicesListItemListener, VirtualDeviceList(model), DeviceItem.Companion.Type.Normal))

        }

        override fun getDeviceListView(): VirtualListView {
            return deviceListView
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(devicesUIObserver, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(devicesUIObserver, untrustedDeviceInfo)
        }

        override fun showSyncDeviceAuthConfirmation(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(syncAuthDialog.isShowing() != null && syncAuthDialog.isShowing() == false)
                syncAuthDialog.showLinkDeviceAuthDialog(devicesUIObserver, trustedDeviceInfo)
            else if(syncAuthDialog.isShowing() == null)
                syncAuthDialog.showLinkDeviceAuthDialog(devicesUIObserver, trustedDeviceInfo)
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

        override fun showRemoveDeviceDialog(deviceId: Int, position: Int) {
            settingRemoveDeviceDialog.showRemoveDeviceDialog(devicesUIObserver, deviceId, position)
        }

        override fun setRemoveDeviceError(message: UIMessage) {
            settingRemoveDeviceDialog.setPasswordError(message)
        }

        override fun removeDeviceDialogToggleLoad(loading: Boolean) {
            settingRemoveDeviceDialog.toggleLoad(loading)
        }

        override fun removeDeviceDialogDismiss() {

            settingRemoveDeviceDialog.dismissDialog()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun showProgressBar(show: Boolean) {
            deviceLoadProgress.visibility = if(show) View.VISIBLE else View.GONE
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
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