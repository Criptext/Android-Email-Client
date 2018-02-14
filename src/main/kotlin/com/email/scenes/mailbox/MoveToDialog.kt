package com.email.scenes.mailbox

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import com.email.MailboxActivity
import com.email.R

/**
 * Created by sebas on 2/8/18.
 */
class MoveToDialog(val context: Context) {
    private var movetoDialog: AlertDialog? = null
    private val res = context.resources

    fun showMoveToDialog(onMoveThreadsListener: OnMoveThreadsListener) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as MailboxActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_move_to, null)

        dialogBuilder.setView(dialogView)

        movetoDialog = createDialog(dialogView,
                    dialogBuilder,
                    onMoveThreadsListener)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             onMoveThreadsListener: OnMoveThreadsListener
    ): AlertDialog {
        val width = res.getDimension(R.dimen.alert_dialog_moveto_width).toInt()
        val height = res.getDimension(R.dimen.alert_dialog_moveto_height).toInt()
        val newMovetoDialog = dialogBuilder.create()
        newMovetoDialog.show()
        newMovetoDialog.window.setLayout(width, height)

        val drawableBackground = res.getDrawable(R.drawable.dialog_label_chooser_shape)
        newMovetoDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView,
                newMovetoDialog,
                onMoveThreadsListener)

        return newMovetoDialog
    }

    fun assignButtonEvents(view: View,
                           dialog: AlertDialog,
                           onMoveThreadsListener: OnMoveThreadsListener) {

        val btn_spam = view.findViewById(R.id.move_to_spam) as Button
        val btn_trash = view.findViewById(R.id.move_to_trash) as Button
        val btn_cancel = view.findViewById(R.id.move_to_cancel) as Button

        btn_spam.setOnClickListener {
            onMoveThreadsListener.moveToSpam()
            dialog.dismiss()
        }

        btn_trash.setOnClickListener {
            onMoveThreadsListener.moveToTrash()
            dialog.dismiss()
        }

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}
