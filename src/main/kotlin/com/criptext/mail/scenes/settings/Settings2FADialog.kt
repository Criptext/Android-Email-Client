package com.criptext.mail.scenes.settings

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.criptext.mail.R


class Settings2FADialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLogoutDialog(hasRecoveryEmailConfirmed: Boolean) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_2_fa_enable_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, hasRecoveryEmailConfirmed)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             hasRecoveryEmailConfirmed: Boolean): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window.setBackgroundDrawable(drawableBackground)

        val title = dialogView.findViewById(R.id.title_two_fa_dialog) as TextView
        val message1 = dialogView.findViewById(R.id.message_two_fa) as TextView
        val message2 = dialogView.findViewById(R.id.second_message_two_fa) as TextView

        if(hasRecoveryEmailConfirmed){
            title.text = context.getText(R.string.title_enabled_two_fa)
            message1.text = context.getText(R.string.message_enabled_two_fa)
            message2.visibility = View.GONE
        }else{
            title.text = context.getText(R.string.title_warning_two_fa)
            message1.text = context.getText(R.string.message_warning_two_fa)
            message2.visibility = View.VISIBLE
        }

        assignButtonEvents(dialogView, newLogoutDialog)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog) {

        val btnGotIt = view.findViewById(R.id.got_it_button) as Button


        btnGotIt.setOnClickListener {
            dialog.dismiss()
        }
    }
}
