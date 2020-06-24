package com.criptext.mail.scenes.composer.ui

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R

class RecoveryEmailRestrictionDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private var uiObserver: ComposerUIObserver? = null
    private var rootLayout: View? = null
    private var imageView: ImageView? = null
    private var title: TextView? = null
    private var message: TextView? = null
    private var verifyRecoveryEmail: Button? = null
    private var verifyRecoveryEmailProgress: ProgressBar? = null
    private var notNow: TextView? = null

    fun showDialog(mailboxUIObserver: ComposerUIObserver?) {

        uiObserver = mailboxUIObserver

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.recovery_email_restriction_dialog, null)

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


        verifyRecoveryEmail?.setOnClickListener{
            uiObserver?.onVerifyRecoveryEmailPressed()
        }

        notNow?.setOnClickListener{
            dialog?.dismiss()
        }


        return newLogoutDialog
    }

    private fun loadViews(dialogView: View){
        verifyRecoveryEmail = dialogView.findViewById(R.id.verify_button)
        verifyRecoveryEmailProgress = dialogView.findViewById(R.id.verify_progress)
        notNow = dialogView.findViewById(R.id.not_now)
        title = dialogView.findViewById(R.id.backup_recommend_title)
        message = dialogView.findViewById(R.id.backup_recommend_message)
    }

    fun loading(isLoading: Boolean){
        if(isLoading){
            verifyRecoveryEmail?.visibility = View.GONE
            verifyRecoveryEmailProgress?.visibility = View.VISIBLE
        } else {
            verifyRecoveryEmail?.visibility = View.VISIBLE
            verifyRecoveryEmailProgress?.visibility = View.GONE
        }
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
