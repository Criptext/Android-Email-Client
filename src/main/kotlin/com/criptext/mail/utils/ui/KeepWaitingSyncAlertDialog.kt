package com.criptext.mail.utils.ui

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.linking.LinkingUIObserver

class KeepWaitingSyncAlertDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLinkDeviceAuthDialog(observer: LinkingUIObserver?) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.keep_waiting_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(observer, dialogView,
                dialogBuilder)
    }

    private fun createDialog(observer: LinkingUIObserver?, dialogView: View,
                             dialogBuilder: AlertDialog.Builder)
            : AlertDialog? {
        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLinkDeviceAuthDialog = dialogBuilder.create()
        val window = newLinkDeviceAuthDialog.window
        newLinkDeviceAuthDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newLinkDeviceAuthDialog.window.setBackgroundDrawable(drawableBackground)

        newLinkDeviceAuthDialog.setCancelable(false)
        newLinkDeviceAuthDialog.setCanceledOnTouchOutside(false)

        val textView = dialogView.findViewById(R.id.message_text) as TextView
        val imageView = dialogView.findViewById(R.id.imageViewDeviceType) as ImageView


        assignButtonEvents(observer, dialogView,
                newLinkDeviceAuthDialog)

        return newLinkDeviceAuthDialog
    }

    fun isShowing(): Boolean? {
        return dialog?.isShowing
    }

    private fun assignButtonEvents(observer: LinkingUIObserver?, view: View,
                                   dialog: AlertDialog) {

        val btnOk = view.findViewById(R.id.link_auth_yes) as Button

        btnOk.setOnClickListener {
            observer?.onKeepWaitingOk()
            dialog.dismiss()
        }

        val btnCancel = view.findViewById(R.id.link_auth_no) as Button

        btnCancel.setOnClickListener {
            observer?.onKeepWaitingCancel()
            dialog.dismiss()
        }
    }
}
