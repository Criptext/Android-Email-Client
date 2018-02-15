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
    private lateinit var btn_cancel: Button
    private lateinit var btn_spam: Button
    private lateinit var btn_trash: Button

    fun showMoveToDialog(moveToDataSourceHandler: OnMoveThreadsListener) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as MailboxActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_move_to, null)
        val width = context.resources.getDimension(R.dimen.alert_dialog_moveto_width).toInt()
        val height = context.resources.getDimension(R.dimen.alert_dialog_moveto_height).toInt()

        dialogBuilder.setView(dialogView)

        movetoDialog = dialogBuilder.create()
        movetoDialog?.show()
        movetoDialog?.getWindow()?.setLayout(width, height)
        val drawableBackground = context.resources.getDrawable(R.drawable.dialog_label_chooser_shape)
        movetoDialog?.window?.setBackgroundDrawable(drawableBackground)

        initButtons(dialogView)
        assignButtonEvents(movetoDialog!!,
                moveToDataSourceHandler)
    }

    fun initButtons(view: View){
        btn_spam = view.findViewById(R.id.move_to_spam) as Button
        btn_trash = view.findViewById(R.id.move_to_trash) as Button
        btn_cancel = view.findViewById(R.id.move_to_cancel) as Button
    }

    fun assignButtonEvents(dialog: AlertDialog,
                           moveToDataSourceHandler: OnMoveThreadsListener) {
        btn_spam.setOnClickListener {
            moveToDataSourceHandler.moveToSpam()
            dialog.dismiss()
        }

        btn_trash.setOnClickListener {
            moveToDataSourceHandler.moveToTrash()
            dialog.dismiss()
        }

        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}
