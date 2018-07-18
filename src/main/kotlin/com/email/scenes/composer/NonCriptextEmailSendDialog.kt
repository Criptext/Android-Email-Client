package com.email.scenes.composer

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
import com.email.R
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage
import com.email.validation.FormInputState


class NonCriptextEmailSendDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var setPasswordSwitch: Switch
    private lateinit var editTextPasswordLayout: RelativeLayout
    private lateinit var editTextPasswordRepeatLayout: RelativeLayout
    private lateinit var switchOffMessage: TextView

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout
    private lateinit var passwordSuccessImage: ImageView
    private lateinit var passwordErrorImage: ImageView

    private lateinit var confirmPassword: AppCompatEditText
    private lateinit var confirmPasswordInput: TextInputLayout
    private lateinit var confirmPasswordSuccessImage: ImageView
    private lateinit var confirmPasswordErrorImage: ImageView

    private lateinit var btnSend : Button
    private lateinit var btnCancel : Button

    private lateinit var view: View

    var uiObserver: ComposerUIObserver? = null

    fun showNonCriptextEmailSendDialog(observer: ComposerUIObserver?) {

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        view = inflater.inflate(R.layout.non_criptext_email_send_dialog, null)

        dialogBuilder.setView(view)
        uiObserver = observer

        dialog = createDialog(view, dialogBuilder, uiObserver)
    }

    private fun createDialog(view: View, dialogBuilder: AlertDialog.Builder,
                             observer: ComposerUIObserver?): AlertDialog {

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
        assignPasswordTextListener()
        assignConfirmPasswordTextChangeListener()

        setPasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            uiObserver?.setOnCheckedChangeListener(isChecked)
            if(isChecked){
                editTextPasswordLayout.visibility = View.VISIBLE
                editTextPasswordRepeatLayout.visibility = View.VISIBLE
                switchOffMessage.visibility = View.GONE
            }
            else{
                if(!btnSend.isEnabled) enableSendEmailButton()
                editTextPasswordLayout.visibility = View.GONE
                editTextPasswordRepeatLayout.visibility = View.GONE
                switchOffMessage.visibility = View.VISIBLE
            }
        }


        return nonCriptextEmailSendDialog
    }

    private fun initializeLayoutComponents(){
        setPasswordSwitch = view.findViewById(R.id.set_password_switch)

        editTextPasswordLayout = view.findViewById(R.id.edit_text_password_layout)
        editTextPasswordRepeatLayout = view.findViewById(R.id.edit_text_password_repeat_layout)

        switchOffMessage = view.findViewById(R.id.switch_off_message)

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
            disableSendEmailButton()
        }
    }

    fun disableSendEmailButton() {
        btnSend.isEnabled = false
    }

    fun enableSendEmailButton() {
        btnSend.isEnabled = true
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
                                   observer: ComposerUIObserver?) {

        btnSend.setOnClickListener {
                uiObserver?.sendDialogButtonPressed()
                dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
}