package com.email.scenes.mailbox

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.email.R
import com.email.db.models.FullEmail


class DeleteEmailDialog(val context: Context) {
    private var deleteDialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var fullEmailToDelete: FullEmail

    fun showDeleteEmailDialog(onDeleteEmailListener: OnDeleteEmailListener, fullEmail: FullEmail) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.delete_email_warning_dialog, null)

        dialogBuilder.setView(dialogView)

        deleteDialog = createDialog(dialogView,
                dialogBuilder,
                onDeleteEmailListener)
        fullEmailToDelete = fullEmail
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             onDeleteEmailListener: OnDeleteEmailListener
    ): AlertDialog {
        val width = res.getDimension(R.dimen.delete_thread_warning_width).toInt()
        val newRecoveryEmailWarningDialog = dialogBuilder.create()
        newRecoveryEmailWarningDialog.show()

        val window = newRecoveryEmailWarningDialog.window
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)

        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newRecoveryEmailWarningDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView,
                newRecoveryEmailWarningDialog,
                onDeleteEmailListener)

        return newRecoveryEmailWarningDialog
    }

    fun assignButtonEvents(view: View,
                           dialog: AlertDialog,
                           onDeleteEmailListener: OnDeleteEmailListener) {

        val btn_yes = view.findViewById(R.id.delete_thread_warning_yes) as Button
        val btn_no = view.findViewById(R.id.delete_thread_warning_no) as Button

        btn_yes.setOnClickListener {
            onDeleteEmailListener.onDeleteConfirmed(fullEmailToDelete)
            dialog.dismiss()
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
        }
    }
}
