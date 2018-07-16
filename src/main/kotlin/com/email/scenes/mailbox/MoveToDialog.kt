package com.email.scenes.mailbox

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import com.email.BaseActivity
import com.email.R
import com.email.SecureEmail

/**
 * Created by sebas on 2/8/18.
 */
class MoveToDialog(val context: Context) {
    private var movetoDialog: AlertDialog? = null
    private val res = context.resources

    fun showMoveToDialog(onMoveThreadsListener: OnMoveThreadsListener, currentFolder: String) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as BaseActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_move_to, null)

        dialogBuilder.setView(dialogView)

        movetoDialog = createDialog(dialogView,
                    dialogBuilder,
                    onMoveThreadsListener,
                    currentFolder)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             onMoveThreadsListener: OnMoveThreadsListener,
                             currentFolder: String
    ): AlertDialog {
        val width = res.getDimension(R.dimen.alert_dialog_moveto_width).toInt()
        val height = res.getDimension(R.dimen.alert_dialog_moveto_height).toInt()
        val newMovetoDialog = dialogBuilder.create()
        newMovetoDialog.show()
        newMovetoDialog.window.setLayout(width, height)

        val drawableBackground = ContextCompat.getDrawable(dialogView.context, R.drawable.dialog_label_chooser_shape)
        newMovetoDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView,
                newMovetoDialog,
                onMoveThreadsListener,
                currentFolder)

        return newMovetoDialog
    }

    fun assignButtonEvents(view: View,
                           dialog: AlertDialog,
                           onMoveThreadsListener: OnMoveThreadsListener,
                           currentFolder: String) {

        val btn_inbox = view.findViewById(R.id.move_to_inbox) as Button
        val btn_spam = view.findViewById(R.id.move_to_spam) as Button
        val btn_trash = view.findViewById(R.id.move_to_trash) as Button
        val btn_cancel = view.findViewById(R.id.move_to_cancel) as Button

        when(currentFolder){
            SecureEmail.LABEL_ALL_MAIL -> btn_inbox.visibility = View.VISIBLE
            SecureEmail.LABEL_SPAM -> btn_spam.visibility = View.GONE
            SecureEmail.LABEL_TRASH -> btn_trash.visibility = View.GONE
            else -> {
            }
        }

        btn_inbox.setOnClickListener {
            onMoveThreadsListener.onMoveToInboxClicked()
            dialog.dismiss()
        }

        btn_spam.setOnClickListener {
            onMoveThreadsListener.onMoveToSpamClicked()
            dialog.dismiss()
        }

        btn_trash.setOnClickListener {
            onMoveThreadsListener.onMoveToTrashClicked()
            dialog.dismiss()
        }

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}
