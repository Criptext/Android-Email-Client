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
import com.criptext.mail.utils.KeyboardManager

/**
 * Created by sebas on 3/8/18.
 */

class SettingsCustomLabelDialog(val context: Context) {

    private val res = context.resources
    private var dialog: AlertDialog? = null
    private lateinit var editTextLabelName: EditText

    fun showCustomLabelDialog(observer: SettingsUIObserver?, keyboardManager: KeyboardManager) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_custom_label_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer, keyboardManager)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?,
                             keyboardManager: KeyboardManager): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newPasswordLoginDialog = dialogBuilder.create()
        val window = newPasswordLoginDialog.window
        newPasswordLoginDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newPasswordLoginDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView, newPasswordLoginDialog, observer, keyboardManager)
        editTextLabelName = dialogView.findViewById(R.id.edit_text_custom_dialog)
        keyboardManager.showKeyboardWithDelay(editTextLabelName)

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: SettingsUIObserver?,
                                   keyboardManager: KeyboardManager) {

        val btn_yes = view.findViewById(R.id.settings_label_ok) as Button
        val btn_no = view.findViewById(R.id.settings_label_no) as Button

        btn_yes.setOnClickListener {
            if(editTextLabelName.text.toString().isNotEmpty()) {
                observer?.onCustomLabelNameAdded(editTextLabelName.text.toString())
                dialog.dismiss()
            }
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
            keyboardManager.hideKeyboard()
        }
    }
}
