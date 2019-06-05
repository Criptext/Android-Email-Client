package com.criptext.mail.scenes.mailbox.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R

class RestoreBackupDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private var uiObserver: MailboxUIObserver? = null
    private var rootLayout: View? = null
    private var restoreFromBackup: Button? = null

    fun showDialog(mailboxUIObserver: MailboxUIObserver?) {

        uiObserver = mailboxUIObserver

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.restore_backup_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window?.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCanceledOnTouchOutside(false)
        newLogoutDialog.setCancelable(false)
        rootLayout = dialogView.findViewById(R.id.viewRoot)
        loadViews(dialogView)


        restoreFromBackup?.setOnClickListener{
            dialog?.dismiss()
            uiObserver?.restoreFromBackupPressed()
        }

        dialogView.findViewById<TextView>(R.id.skip_restore).setOnClickListener {
            dismiss()
        }


        return newLogoutDialog
    }

    private fun loadViews(dialogView: View){
        restoreFromBackup = dialogView.findViewById(R.id.restore_button)
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
