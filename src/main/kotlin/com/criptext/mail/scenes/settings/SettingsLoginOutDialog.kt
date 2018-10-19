package com.criptext.mail.scenes.settings

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.criptext.mail.R

class SettingsLoginOutDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLoginOutDialog(observer: SettingsUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_custom_login_out_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?): AlertDialog {

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


        return newLogoutDialog
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
