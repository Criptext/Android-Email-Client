package com.criptext.mail.utils.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.uiobserver.UIObserver

class SyncDeviceAlertDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLinkDeviceAuthDialog(observer: UIObserver?, trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.link_device_auth_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(observer, dialogView,
                dialogBuilder, trustedDeviceInfo)
    }

    private fun createDialog(observer: UIObserver?, dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
            : AlertDialog? {
        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLinkDeviceAuthDialog = dialogBuilder.create()
        val window = newLinkDeviceAuthDialog.window
        newLinkDeviceAuthDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newLinkDeviceAuthDialog.window?.setBackgroundDrawable(drawableBackground)

        newLinkDeviceAuthDialog.setCancelable(false)
        newLinkDeviceAuthDialog.setCanceledOnTouchOutside(false)

        val textView = dialogView.findViewById(R.id.message_text) as TextView
        val accountTextView = dialogView.findViewById(R.id.account_email) as TextView
        val imageView = dialogView.findViewById(R.id.imageViewDeviceType) as ImageView
        accountTextView.text = trustedDeviceInfo.recipientId.plus("@${trustedDeviceInfo.domain}")
        textView.text = context.getString(R.string.link_auth_message,
                trustedDeviceInfo.deviceFriendlyName)
        when(trustedDeviceInfo.deviceType){
            DeviceUtils.DeviceType.PC, DeviceUtils.DeviceType.MacStore, DeviceUtils.DeviceType.MacInstaller,
            DeviceUtils.DeviceType.WindowsInstaller, DeviceUtils.DeviceType.WindowsStore,
            DeviceUtils.DeviceType.LinuxInstaller -> imageView.setImageResource(R.drawable.ic_laptoplimit)
            else -> imageView.setImageResource(R.drawable.ic_mobilelimit)
        }

        assignButtonEvents(observer, dialogView,
                newLinkDeviceAuthDialog, trustedDeviceInfo)

        return newLinkDeviceAuthDialog
    }

    fun isShowing(): Boolean? {
        return dialog?.isShowing
    }

    fun dismiss(){
        dialog?.dismiss()
    }

    private fun assignButtonEvents(observer: UIObserver?, view: View,
                                   dialog: AlertDialog, trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {

        val btnOk = view.findViewById(R.id.link_auth_yes) as Button

        btnOk.setOnClickListener {
            observer?.onSyncAuthConfirmed(trustedDeviceInfo)
            dialog.dismiss()
        }

        val btnCancel = view.findViewById(R.id.link_auth_no) as Button

        btnCancel.setOnClickListener {
            observer?.onSyncAuthDenied(trustedDeviceInfo)
            dialog.dismiss()
        }
    }
}
