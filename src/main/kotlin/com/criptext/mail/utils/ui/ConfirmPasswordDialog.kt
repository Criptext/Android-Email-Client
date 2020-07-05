package com.criptext.mail.utils.ui

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.uiobserver.UIObserver

class ConfirmPasswordDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var progressBar: ProgressBar
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout
    private lateinit var passwordSuccessImage: ImageView
    private lateinit var passwordErrorImage: ImageView

    private lateinit var view: View

    fun showDialog(observer: UIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.remote_change_confirm_pasword_dialog, null)

        dialogBuilder.setView(view)

        dialog = createDialog(view, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: UIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window?.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCancelable(false)
        newLogoutDialog.setCanceledOnTouchOutside(false)

        password = dialogView.findViewById(R.id.input) as AppCompatEditText
        passwordInput = dialogView.findViewById(R.id.input_layout)
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))
        passwordSuccessImage = dialogView.findViewById(R.id.success)
        passwordErrorImage = dialogView.findViewById(R.id.error)

        assignPasswordTextListener()
        assignButtonEvents(dialogView, newLogoutDialog, observer)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: UIObserver?) {

        btnOk = view.findViewById(R.id.change_email_ok) as Button
        btnCancel = view.findViewById(R.id.change_email_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnOk.setOnClickListener {
            toggleLoad(true)
            observer?.onOkButtonPressed(password.text.toString())
        }

        btnCancel.setOnClickListener {
            observer?.onCancelButtonPressed()
            dialog.dismiss()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hidePasswordError() {
        passwordErrorImage.visibility = View.GONE

        passwordInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    private fun showPasswordError(message: UIMessage) {
        passwordErrorImage.visibility = View.VISIBLE

        passwordInput.error = view.context.getLocalizedUIMessage(message)
    }

    fun setPasswordError(message: UIMessage?) {
        if (message == null) {
            hidePasswordError()
            enableSaveButton()
        } else {
            showPasswordError(message)
            disableSaveButton()
        }
    }

    fun disableSaveButton() {
        btnOk.isEnabled = false
    }

    fun enableSaveButton() {
        btnOk.isEnabled = true
    }

    private fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                setPasswordError(null)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun toggleLoad(loading: Boolean){
        if(loading){
            progressBar.visibility = View.VISIBLE
            btnOk.visibility = View.GONE
            btnCancel.visibility = View.GONE
        }else{
            progressBar.visibility = View.GONE
            btnOk.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }
}
