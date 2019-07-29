package com.criptext.mail.utils.ui

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.data.DialogData

class GeneralMessageOkDialog(val context: Context, private val dialogData: DialogData.DialogMessageData) {
    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showDialog(messages: List<UIMessage>? = null) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.forgot_password_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView,
                dialogBuilder, messages)
    }

    private fun createDialog(dialogView: View,
                             dialogBuilder: AlertDialog.Builder,
                             messages: List<UIMessage>?)
            : AlertDialog {
        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newPasswordLoginDialog = dialogBuilder.create()
        val window = newPasswordLoginDialog.window
        newPasswordLoginDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(
                dialogView.context, R.drawable.dialog_label_chooser_shape)
        newPasswordLoginDialog.window?.setBackgroundDrawable(drawableBackground)

        val messageTextView = dialogView.findViewById(R.id.message_text) as TextView
        val titleTextView = dialogView.findViewById(R.id.forgot_title) as TextView
        val dialogMessage = messages?.firstOrNull() ?: dialogData.message.first()

        titleTextView.text = context.getLocalizedUIMessage(dialogData.title)
        messageTextView.text = context.getLocalizedUIMessage(dialogMessage)

        assignButtonEvents(dialogView,
                newPasswordLoginDialog)

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View,
                                   dialog: AlertDialog) {

        val btnOk = view.findViewById(R.id.reset_password_ok) as Button

        btnOk.setOnClickListener {
            dialog.dismiss()
            dialogData.onOkPress()
        }
    }
}
