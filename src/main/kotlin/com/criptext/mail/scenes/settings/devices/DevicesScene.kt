package com.criptext.mail.scenes.settings.devices

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.SettingsRemoveDeviceDialog
import com.criptext.mail.scenes.settings.devices.data.DeviceAdapter
import com.criptext.mail.scenes.settings.devices.data.VirtualDeviceList
import com.criptext.mail.scenes.settings.recovery_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.ForgotPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
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
    fun getDeviceListView(): VirtualListView
    fun showRemoveDeviceDialog(deviceId: Int, position: Int)
    fun removeDeviceDialogToggleLoad(loading: Boolean)
    fun setRemoveDeviceError(message: UIMessage)
    fun removeDeviceDialogDismiss()

    class Default(val view: View): DevicesScene{
        private lateinit var devicesUIObserver: DevicesUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val recyclerViewDevices: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.recyclerViewDevices)
        }
        private val deviceListView: VirtualListView = VirtualRecyclerView(recyclerViewDevices)

        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val settingRemoveDeviceDialog = SettingsRemoveDeviceDialog(context)

        override fun attachView(devicesUIObserver: DevicesUIObserver, keyboardManager: KeyboardManager,
                                model: DevicesModel, devicesListItemListener: DevicesListItemListener) {
            this.devicesUIObserver = devicesUIObserver

            backButton.setOnClickListener {
                this.devicesUIObserver.onBackButtonPressed()
            }

            deviceListView.setAdapter(DeviceAdapter(view.context, devicesListItemListener, VirtualDeviceList(model)))

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