package com.criptext.mail.scenes.mailbox.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class RecommendBackupDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private var uiObserver: MailboxUIObserver? = null
    private var rootLayout: View? = null
    private var imageView: ImageView? = null
    private var title: TextView? = null
    private var message: TextView? = null
    private var turnOnAutoBackup: Button? = null
    private var notNow: TextView? = null

    fun showDialog(mailboxUIObserver: MailboxUIObserver?) {

        uiObserver = mailboxUIObserver

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.recommend_backup_dialog, null)

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


        turnOnAutoBackup?.setOnClickListener{
            dialog?.dismiss()
            uiObserver?.turnOnAutoBackup()
        }

        notNow?.setOnClickListener{
            notNowFirst(dialogView)
        }


        return newLogoutDialog
    }

    private fun notNowFirst(view: View){
        imageView?.visibility = View.GONE
        title?.text = view.context.getLocalizedUIMessage(UIMessage(R.string.password_warning_dialog_title))
        message?.text = view.context.getLocalizedUIMessage(UIMessage(R.string.recommend_backup_warning_message))
        notNow?.text = view.context.getLocalizedUIMessage(UIMessage(R.string.recommend_backup_warning_cancel))
        notNow?.setOnClickListener{
            dismiss()
            uiObserver?.notNowAutoBackup()
        }
    }

    private fun loadViews(dialogView: View){
        turnOnAutoBackup = dialogView.findViewById(R.id.turn_on)
        notNow = dialogView.findViewById(R.id.not_now)
        title = dialogView.findViewById(R.id.backup_recommend_title)
        message = dialogView.findViewById(R.id.backup_recommend_message)
        imageView = dialogView.findViewById(R.id.backup_recommend_image)
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
