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

/**
 * Created by sebas on 3/8/18.
 */

class SettingsRemovingDeviceDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showLoginOutDialog(observer: SettingsUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_removing_device_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newRemovingDeviceDialog = dialogBuilder.create()
        val window = newRemovingDeviceDialog.window
        newRemovingDeviceDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newRemovingDeviceDialog.window.setBackgroundDrawable(drawableBackground)
        newRemovingDeviceDialog.setCanceledOnTouchOutside(false)

        return newRemovingDeviceDialog
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}
