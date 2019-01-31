package com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.github.omadahealth.lollipin.lib.managers.AppLockActivity

class MessageAndProgressDialog(val context: Context, val message: UIMessage) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showDialog() {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as BaseActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.message_and_progress_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCanceledOnTouchOutside(false)
        dialogView.findViewById<TextView>(R.id.message).text = context.getLocalizedUIMessage(message)


        return newLogoutDialog
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
