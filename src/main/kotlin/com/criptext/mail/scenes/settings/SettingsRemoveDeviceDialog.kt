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

class SettingsRemoveDeviceDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    fun showRemoveDeviceDialog(observer: SettingsUIObserver?, deviceId: Int, position: Int) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_remove_device_dialog, null)

        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)

        dialog = createDialog(dialogView, dialogBuilder, observer, deviceId, position)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?, deviceId: Int, position: Int): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newRemoveDeviceDialog = dialogBuilder.create()
        val window = newRemoveDeviceDialog.window
        newRemoveDeviceDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newRemoveDeviceDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView, newRemoveDeviceDialog, observer, deviceId, position)


        return newRemoveDeviceDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: SettingsUIObserver?, deviceId: Int, position: Int) {

        val btn_yes = view.findViewById(R.id.settings_remove_yes) as Button
        val btn_no = view.findViewById(R.id.settings_remove_cancel) as Button

        btn_yes.setOnClickListener {
            dialog.dismiss()
            observer?.onRemoveDeviceConfirmed(deviceId, position)
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
        }
    }
}
