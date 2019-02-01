package com.criptext.mail.scenes.settings.profile.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.profile.ProfileUIObserver

/**
 * Created by sebas on 3/8/18.
 */

class ProfileNameDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var editTextFullName: EditText
    private lateinit var btn_yes: Button

    fun showProfileNameDialog(fullName: String, observer: ProfileUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.settings_profile_name_dialog, null)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer, fullName)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: ProfileUIObserver?,
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

        editTextFullName.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btn_yes.isEnabled = (editTextFullName.text.toString().isNotEmpty() && editTextFullName.text.toString() != fullName)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        return newPasswordLoginDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: ProfileUIObserver?) {

        btn_yes = view.findViewById(R.id.settgins_profile_ok) as Button
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
