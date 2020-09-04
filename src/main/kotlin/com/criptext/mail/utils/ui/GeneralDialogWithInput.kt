package com.criptext.mail.utils.ui

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.criptext.mail.R
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.google.android.material.textfield.TextInputLayout

class GeneralDialogWithInput(val context: Context, val data: DialogData) {

    private var dialog: AlertDialog? = null
    private val res = context.resources
    lateinit var editTextEmail: AppCompatEditText
    lateinit var editTextEmailLayout: TextInputLayout
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    fun showDialog(observer: UIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.general_dialog_with_input, null)
        dialogView.findViewById<TextView>(R.id.title).text = context.getLocalizedUIMessage(data.title)

        dialogBuilder.setView(dialogView)

        dialog = createDialog(dialogView, dialogBuilder, observer)
    }

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder,
                             observer: UIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.password_login_dialog_width).toInt()
        val newPasswordLoginDialog = dialogBuilder.create()
        val window = newPasswordLoginDialog.window
        newPasswordLoginDialog.show()
        window?.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(dialogView.context,
                R.drawable.dialog_label_chooser_shape)
        newPasswordLoginDialog.window?.setBackgroundDrawable(drawableBackground)

        assignButtonEvents(dialogView, newPasswordLoginDialog, observer)
        editTextEmail = dialogView.findViewById(R.id.input)
        editTextEmailLayout = dialogView.findViewById(R.id.input_layout)
        setData(dialogView)

        textListener()

        return newPasswordLoginDialog
    }

    private fun setData(dialogView: View){
        if(data is DialogData.DialogDataForReplyToEmail) {
            if (data.replyToEmail != null) {
                editTextEmail.setText(data.replyToEmail)
                editTextEmail.setSelection(data.replyToEmail.length)
            }
        } else if(data is DialogData.DialogDataForRecoveryCode){
            dialogView.findViewById<TextView>(R.id.message).visibility = View.VISIBLE
            dialogView.findViewById<TextView>(R.id.message).text = context.getLocalizedUIMessage(data.message)
        } else if (data is DialogData.DialogDataForInput){
            if(data.input != null) {
                editTextEmail.setText(data.input)
                editTextEmail.setSelection(data.input.length)
            }
        }
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: UIObserver?) {

        btnOk = view.findViewById(R.id.settgins_profile_ok) as Button
        btnCancel = view.findViewById(R.id.settgins_profile_no) as Button
        progressBar = view.findViewById(R.id.check_password_progress) as ProgressBar

        btnOk.setOnClickListener {
            if(editTextEmail.text.toString().isNotEmpty()) {
                observer?.onGeneralOkButtonPressed(createResult())
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun hidePasswordError() {
        editTextEmailLayout.error = ""
    }

    private fun showPasswordError(message: UIMessage) {
        editTextEmailLayout.error = context.getLocalizedUIMessage(message)
    }

    fun setEmailError(message: UIMessage?) {
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

    private fun textListener() {
        editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (data is DialogData.DialogDataForReplyToEmail) {
                    val userInput = AccountDataValidator.validateEmailAddress(text.toString())
                    when (userInput) {
                        is FormData.Valid -> {
                            if (!text.isNullOrEmpty() && text.toString() != data.replyToEmail) {
                                setEmailError(null)
                                enableSaveButton()
                            } else {
                                disableSaveButton()
                            }
                        }
                        is FormData.Error -> {
                            disableSaveButton()
                            setEmailError(userInput.message)
                        }
                    }
                } else if(data is DialogData.DialogDataForRecoveryCode){
                    val userInput = AccountDataValidator.validateRecoveryCode(text.toString())
                    when (userInput) {
                        is FormData.Valid -> {
                            if (!text.isNullOrEmpty()) {
                                setEmailError(null)
                                enableSaveButton()
                            } else {
                                disableSaveButton()
                            }
                        }
                        is FormData.Error -> {
                            disableSaveButton()
                            setEmailError(userInput.message)
                        }
                    }
                } else if(data is DialogData.DialogDataForInput){
                    val userInput = text.toString().trim()
                    when (userInput) {
                        "" -> {
                            disableSaveButton()
                            setEmailError(UIMessage(R.string.fullname_empty_error))
                        }
                        else -> {
                            if (!text.isNullOrEmpty()) {
                                setEmailError(null)
                                enableSaveButton()
                            } else {
                                disableSaveButton()
                            }
                        }
                    }
                }
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

    fun dismiss(){
        dialog?.dismiss()
    }

    private fun createResult(): DialogResult {
        return when(data.type){
            is DialogType.DeleteAccount ->
                DialogResult.DialogWithInput("", data.type)
            is DialogType.DeleteLabel,
            is DialogType.SignIn,
            is DialogType.Message,
            is DialogType.ManualSyncConfirmation,
            is DialogType.UpdateApp,
            is DialogType.SwitchAccount ->
                DialogResult.DialogConfirmation(data.type)
            is DialogType.ReplyToChange,
            is DialogType.RecoveryCode,
            is DialogType.EditLabel ->
                DialogResult.DialogWithInput(editTextEmail.text.toString(), data.type)
            is DialogType.CriptextPlus ->
                DialogResult.DialogCriptextPlus(data.type)
        }
    }
}
