package com.criptext.mail.scenes.import_mailbox

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

    fun showPasswordLoginDialog(importMailboxUIObserver: ImportMailboxUIObserver?) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.password_login_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView,
                dialogBuilder, importMailboxUIObserver)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             importMailboxUIObserver: ImportMailboxUIObserver?
    )
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
                newPasswordLoginDialog, importMailboxUIObserver)

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View,
                                   dialog: AlertDialog, importMailboxUIObserver: ImportMailboxUIObserver?) {

        val btnContinue = view.findViewById(R.id.password_login_yes) as Button
        val btnCancel = view.findViewById(R.id.password_login_no) as Button

        btnContinue.setOnClickListener {
            importMailboxUIObserver?.onSkipContinuePressed()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}
