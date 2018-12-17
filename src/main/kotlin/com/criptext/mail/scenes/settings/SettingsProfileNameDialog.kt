package com.criptext.mail.scenes.settings

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.criptext.mail.R

/**
 * Created by sebas on 3/8/18.
 */

class SettingsProfileNameDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var editTextFullName: EditText

    fun showProfileNameDialog(fullName: String, observer: SettingsUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_profile_name_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer, fullName)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?,
                             fullName: String): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newPasswordLoginDialog = dialogBuilder.create()
        val window = newPasswordLoginDialog.window
        newPasswordLoginDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newPasswordLoginDialog.window.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView, newPasswordLoginDialog, observer)
        editTextFullName = dialogView.findViewById(R.id.edit_text_profile_name)
        editTextFullName.setText(fullName)
        editTextFullName.setSelection(fullName.length)

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: SettingsUIObserver?) {

        val btn_yes = view.findViewById(R.id.settgins_profile_ok) as Button
        val btn_no = view.findViewById(R.id.settgins_profile_no) as Button

        btn_yes.setOnClickListener {
            if(editTextFullName.text.toString().isNotEmpty()) {
                observer?.onProfileNameChanged(editTextFullName.text.toString())
                dialog.dismiss()
            }
        }

        btn_no.setOnClickListener {
            dialog.dismiss()
        }
    }
}
