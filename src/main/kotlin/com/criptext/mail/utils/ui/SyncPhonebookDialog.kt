package com.criptext.mail.utils.ui

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver

class SyncPhonebookDialog(val context: Context) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showSyncPhonebookDialog(mailboxUIObserver: MailboxUIObserver?) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.sync_phonebook_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(mailboxUIObserver, dialogView,
                dialogBuilder)
    }

    private fun createDialog(mailboxUIObserver: MailboxUIObserver?,
                             dialogView: View,
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


        assignButtonEvents(mailboxUIObserver, dialogView,
                newLinkDeviceAuthDialog)

        return newLinkDeviceAuthDialog
    }

    fun isShowing(): Boolean? {
        return dialog?.isShowing
    }

    private fun assignButtonEvents(mailboxUIObserver: MailboxUIObserver?,
                                   view: View,
                                   dialog: AlertDialog) {

        val btnOk = view.findViewById(R.id.link_auth_yes) as Button

        btnOk.setOnClickListener {
            mailboxUIObserver?.onSyncPhoneBookYes()
            dialog.dismiss()
        }

        val btnCancel = view.findViewById(R.id.link_auth_no) as Button

        btnCancel.setOnClickListener {
            mailboxUIObserver?.onStartGuideEmail()
            dialog.dismiss()
        }
    }
}
