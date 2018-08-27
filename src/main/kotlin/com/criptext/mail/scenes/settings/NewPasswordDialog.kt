package com.criptext.mail.scenes.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
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
import com.criptext.mail.scenes.composer.ui.ComposerUIObserver
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.FormInputState


class NewPasswordDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var oldPassword: AppCompatEditText
    private lateinit var oldPasswordInput: TextInputLayout
    private lateinit var oldPasswordSuccessImage: ImageView
    private lateinit var oldPasswordErrorImage: ImageView

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout
    private lateinit var passwordSuccessImage: ImageView
    private lateinit var passwordErrorImage: ImageView

    private lateinit var confirmPassword: AppCompatEditText
    private lateinit var confirmPasswordInput: TextInputLayout
    private lateinit var confirmPasswordSuccessImage: ImageView
    private lateinit var confirmPasswordErrorImage: ImageView

    private var lastUsedPassword: String = ""

    private lateinit var btnSend : Button
    private lateinit var btnCancel : Button

    private lateinit var view: View

    var uiObserver: SettingsUIObserver? = null

    fun showDialog(observer: SettingsUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.change_password_dialog, null)

        dialogBuilder.setView(view)
        uiObserver = observer

        dialog = createDialog(view, dialogBuilder, uiObserver)
    }

    private fun createDialog(view: View, dialogBuilder: AlertDialog.Builder,
                             observer: SettingsUIObserver?): AlertDialog {

        val width = res.getDimension(R.dimen.non_criptext_email_send_dialog_width).toInt()
        val nonCriptextEmailSendDialog = dialogBuilder.create()
        val window = nonCriptextEmailSendDialog.window
        nonCriptextEmailSendDialog.show()
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)
        val drawableBackground = ContextCompat.getDrawable(view.context,
                R.drawable.dialog_label_chooser_shape)
        nonCriptextEmailSendDialog.window.setBackgroundDrawable(drawableBackground)

        initializeLayoutComponents()

        //Assign event listeners
        assignButtonEvents(view, nonCriptextEmailSendDialog, observer)
        assignOldPasswordTextListener()
        assignPasswordTextListener()
        assignConfirmPasswordTextChangeListener()


        return nonCriptextEmailSendDialog
    }

    private fun initializeLayoutComponents(){
        oldPassword = view.findViewById(R.id.old_password)
        oldPasswordInput = view.findViewById(R.id.password_old_input)
        oldPasswordSuccessImage = view.findViewById(R.id.success_old_password)
        oldPasswordErrorImage = view.findViewById(R.id.error_old_password)

        password = view.findViewById(R.id.password)
        passwordInput = view.findViewById(R.id.password_input)
        passwordSuccessImage = view.findViewById(R.id.success_password)
        passwordErrorImage = view.findViewById(R.id.error_password)

        confirmPassword = view.findViewById(R.id.password_repeat)
        confirmPasswordInput = view.findViewById(R.id.password_repeat_input)
        confirmPasswordSuccessImage = view.findViewById(R.id.success_password_repeat)
        confirmPasswordErrorImage = view.findViewById(R.id.error_password_repeat)

        btnSend = (view.findViewById(R.id.non_criptext_email_send) as Button)
        btnCancel = (view.findViewById(R.id.non_criptext_email_cancel) as Button)
    }

    private fun hideOldPasswordError() {
        oldPasswordErrorImage.visibility = View.GONE

        oldPasswordInput.error = ""
    }

    private fun showOldPasswordError(message: UIMessage) {
        oldPasswordErrorImage.visibility = View.VISIBLE

        oldPasswordInput.error = view.context.getLocalizedUIMessage(message)
    }

    fun setOldPasswordError(message: UIMessage?) {
        if (message == null) {
            hideOldPasswordError()
            btnSend.isEnabled = true
        }
        else {
            showOldPasswordError(message)
            btnSend.isEnabled = false
        }
    }

    private fun showPasswordSuccess() {
        passwordSuccessImage.visibility = View.VISIBLE
        confirmPasswordSuccessImage.visibility = View.VISIBLE
    }

    private fun hidePasswordSuccess() {
        passwordSuccessImage.visibility = View.GONE
        confirmPasswordSuccessImage.visibility = View.GONE
    }

    @SuppressLint("RestrictedApi")
    private fun hidePasswordError() {
        passwordErrorImage.visibility = View.GONE
        confirmPasswordErrorImage.visibility = View.GONE

        confirmPasswordInput.error = ""
    }

    @SuppressLint("RestrictedApi")
    private fun showPasswordError(message: UIMessage) {
        passwordErrorImage.visibility = View.VISIBLE
        confirmPasswordErrorImage.visibility = View.VISIBLE

        confirmPasswordInput.error = view.context.getLocalizedUIMessage(message)
    }

    fun setPasswordError(message: UIMessage?) {
        if (message == null) hidePasswordError()
        else {
            showPasswordError(message)
            disableSaveButton()
        }
    }

    fun disableSaveButton() {
        btnSend.isEnabled = false
    }

    fun enableSaveButton() {
        btnSend.isEnabled = true
    }

    private fun assignOldPasswordTextListener() {
        oldPassword.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(text.toString() != lastUsedPassword)
                    setOldPasswordError(null)
                else
                    setOldPasswordError(UIMessage(R.string.password_enter_error))
                uiObserver?.onOldPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    private fun assignPasswordTextListener() {
        password.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    private fun assignConfirmPasswordTextChangeListener() {
        confirmPassword.addTextChangedListener( object : TextWatcher {
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                uiObserver?.onConfirmPasswordChangedListener(text.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun togglePasswordSuccess(show: Boolean) {
        if(show) {
            showPasswordSuccess()
        } else {
            hidePasswordSuccess()
        }
    }



    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: SettingsUIObserver?) {

        btnSend.setOnClickListener {
                uiObserver?.onOkChangePasswordDialogButton()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun dismissDialog(){
        dialog?.dismiss()
    }
}