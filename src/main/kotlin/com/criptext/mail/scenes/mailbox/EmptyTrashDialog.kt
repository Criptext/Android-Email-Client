package com.criptext.mail.scenes.mailbox

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.criptext.mail.R

class EmptyTrashDialog(val context: Context) {
    private var deleteDialog: AlertDialog? = null
    private val res = context.resources

    fun showEmptyTrashDialog(onEmptyTrashListener: OnEmptyTrashListener) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.empty_trash_warning_dialog, null)

        dialogBuilder.setView(dialogView)

        deleteDialog = createDialog(dialogView,
                dialogBuilder,
                onEmptyTrashListener)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             onDeleteThreadListener: OnEmptyTrashListener
    ): AlertDialog {
        val width = res.getDimension(R.dimen.delete_thread_warning_width).toInt()
        val newEmptyTrashDialog = dialogBuilder.create()
        newEmptyTrashDialog.show()

        val window = newEmptyTrashDialog.window
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)

        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newEmptyTrashDialog.window?.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView,
                newEmptyTrashDialog,
                onDeleteThreadListener)

        return newEmptyTrashDialog
    }

    fun assignButtonEvents(view: View,
                           dialog: AlertDialog,
                           onDeleteThreadListener: OnEmptyTrashListener) {

        val btn_yes = view.findViewById(R.id.delete_thread_warning_yes) as Button
        val btn_no = view.findViewById(R.id.delete_thread_warning_no) as Button

        btn_yes.setOnClickListener {
            onDeleteThreadListener.onDeleteConfirmed()
            dialog.dismiss()
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
        }
    }
}
