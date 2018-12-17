package com.criptext.mail.utils.ui

import android.content.Context
import android.os.CountDownTimer
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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver

class GeneralDialogWithInputPassword(val context: Context, val data: DialogData.DialogMessageData) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout

    private lateinit var view: View

    private var isWaiting = true

    fun showDialog(observer: UIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.general_dialog_with_password_input, null)
        view.findViewById<TextView>(R.id.title).text = context.getLocalizedUIMessage(data.title)
        view.findViewById<TextView>(R.id.message).text = context.getLocalizedUIMessage(data.message.first())

        dialogBuilder.setView(view)

        dialog = createDialog(view, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: UIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newLogoutDialog = dialogBuilder.create()
        val window = newLogoutDialog.window
        newLogoutDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newLogoutDialog.window.setBackgroundDrawable(drawableBackground)
        newLogoutDialog.setCancelable(false)
        newLogoutDialog.setCanceledOnTouchOutside(false)

        password = dialogView.findViewById(R.id.input) as AppCompatEditText
        passwordInput = dialogView.findViewById(R.id.input_layout)
        passwordInput.isPasswordVisibilityToggleEnabled = true
        passwordInput.setPasswordVisibilityToggleTintList(
                AppCompatResources.getColorStateList(context, R.color.non_criptext_email_send_eye))

        assignPasswordTextListener()
        assignButtonEvents(dialogView, newLogoutDialog, observer)


        return newLogoutDialog
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: UIObserver?) {

        btnOk = view.findViewById(R.id.btn_ok) as Button
        btnCancel = view.findViewById(R.id.btn_cancel) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnOk.setOnClickListener {
            observer?.onGeneralOkButtonPressed(createResult())
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun createResult(): DialogResult {
        return when(data.type){
            is DialogType.DeleteAccount ->
                DialogResult.DialogWithInput(password.text.toString(), data.type)
            is DialogType.ManualSyncConfirmation ->
                DialogResult.DialogConfirmation(data.type)
        }
    }

    private fun hidePasswordError() {
        passwordInput.error = ""
    }

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

    private fun disableSaveButton() {
        btnOk.isEnabled = false
    }

    private fun enableSaveButton() {
        btnOk.isEnabled = true
    }

    private fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(!isWaiting)
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
