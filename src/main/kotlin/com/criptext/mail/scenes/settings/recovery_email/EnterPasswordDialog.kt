package com.criptext.mail.scenes.settings.recovery_email

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class EnterPasswordDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var progressBar: ProgressBar
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout

    private lateinit var forgotPassword: TextView

    private lateinit var view: View

    fun showDialog(observer: RecoveryEmailUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.recovery_change_email_dialog, null)

        dialogBuilder.setView(view)

        dialog = createDialog(view, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: RecoveryEmailUIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window.setBackgroundDrawable(drawableBackground)

        password = dialogView.findViewById(R.id.input) as AppCompatEditText
        passwordInput = dialogView.findViewById(R.id.input_layout)

        assignPasswordTextListener()
        assignButtonEvents(dialogView, newLogoutDialog, observer)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: RecoveryEmailUIObserver?) {

        btnOk = view.findViewById(R.id.change_email_ok) as Button
        btnCancel = view.findViewById(R.id.change_email_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar
        forgotPassword = view.findViewById(R.id.forgot_password)

        forgotPassword.setOnClickListener {
            dismissDialog()
            observer?.onForgotPasswordPressed()
        }

        btnOk.setOnClickListener {
            observer?.onEnterPasswordOkPressed(password.text.toString())
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hidePasswordError() {
        passwordInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    private fun showPasswordError(message: UIMessage) {
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
