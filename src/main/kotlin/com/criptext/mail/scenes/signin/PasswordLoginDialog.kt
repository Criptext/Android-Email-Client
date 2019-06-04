package com.criptext.mail.scenes.signin

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R

/**
 * Created by sebas on 3/8/18.
 */

class PasswordLoginDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources
    private var username: String = ""

    fun showPasswordLoginDialog(username: String,
            onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.password_login_dialog, null)

        dialogBuilder.setView(dialogView)

        this.username = username
        dialog = createDialog(dialogView,
                dialogBuilder,
                onPasswordLoginDialogListener)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             onPasswordLoginDialogListener: OnPasswordLoginDialogListener)
            : AlertDialog {
        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newPasswordLoginDialog = dialogBuilder.create()
        val window = newPasswordLoginDialog.window
        newPasswordLoginDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newPasswordLoginDialog.window?.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView,
                newPasswordLoginDialog,
                onPasswordLoginDialogListener)

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View,
                                   dialog: AlertDialog,
                                   onPasswordLoginDialogListener: OnPasswordLoginDialogListener) {

        val btn_yes = view.findViewById(R.id.password_login_yes) as Button
        val btn_no = view.findViewById(R.id.password_login_no) as Button

        btn_yes.setOnClickListener {
            onPasswordLoginDialogListener.acceptPasswordLogin(username)
            dialog.dismiss()
        }

        btn_no.setOnClickListener {
            onPasswordLoginDialogListener.cancelPasswordLogin()
            dialog.dismiss()
        }
    }
}
