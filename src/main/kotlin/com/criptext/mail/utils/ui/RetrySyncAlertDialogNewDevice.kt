package com.criptext.mail.utils.ui

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
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.scenes.signin.data.SignInResult

class RetrySyncAlertDialogNewDevice(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLinkDeviceAuthDialog(signInUIObserver: SignInSceneController.SignInUIObserver?, result: SignInResult) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.keep_waiting_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(signInUIObserver, dialogView,
                dialogBuilder, result)
    }

    private fun createDialog(signInUIObserver: SignInSceneController.SignInUIObserver?,
                             dialogView: View,
                             dialogBuilder: AlertDialog.Builder, result: SignInResult)
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


        assignButtonEvents(signInUIObserver, dialogView,
                newLinkDeviceAuthDialog, result)

        return newLinkDeviceAuthDialog
    }

    fun isShowing(): Boolean? {
        return dialog?.isShowing
    }

    private fun assignButtonEvents(signInUIObserver: SignInSceneController.SignInUIObserver?,
                                   view: View,
                                   dialog: AlertDialog, result: SignInResult) {

        val btnOk = view.findViewById(R.id.link_auth_yes) as Button

        btnOk.setOnClickListener {
            signInUIObserver?.onRetrySyncOk(result)
            dialog.dismiss()
        }

        val btnCancel = view.findViewById(R.id.link_auth_no) as Button

        btnCancel.setOnClickListener {
            signInUIObserver?.onRetrySyncCancel()
            dialog.dismiss()
        }
    }
}
