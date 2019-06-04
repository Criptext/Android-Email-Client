package com.criptext.mail.scenes.composer.ui

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.scenes.linking.LinkingUIObserver

class StayInComposerDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLinkDeviceAuthDialog(observer: ComposerUIObserver?) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.stay_in_composer_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(observer, dialogView,
                dialogBuilder)
    }

    private fun createDialog(observer: ComposerUIObserver?, dialogView: View,
                             dialogBuilder: AlertDialog.Builder)
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


        assignButtonEvents(observer, dialogView,
                newLinkDeviceAuthDialog)

        return newLinkDeviceAuthDialog
    }

    fun isShowing(): Boolean? {
        return dialog?.isShowing
    }

    private fun assignButtonEvents(observer: ComposerUIObserver?, view: View,
                                   dialog: AlertDialog) {

        val btnOk = view.findViewById(R.id.stay_yes) as Button

        btnOk.setOnClickListener {
            observer?.leaveComposer()
            dialog.dismiss()
        }

        val btnCancel = view.findViewById(R.id.stay_cancel) as Button

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}
