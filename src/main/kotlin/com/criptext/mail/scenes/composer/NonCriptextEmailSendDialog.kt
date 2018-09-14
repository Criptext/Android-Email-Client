package com.criptext.mail.scenes.composer

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


class NonCriptextEmailSendDialog(val context: Context) {

    private var dialog: AlertDialog? = null
    private val res = context.resources

    private lateinit var setPasswordSwitch: Switch
    private lateinit var editTextPasswordLayout: RelativeLayout
    private lateinit var switchOffMessage: TextView
    private lateinit var setPassphraseText: TextView

    private lateinit var password: AppCompatEditText
    private lateinit var passwordInput: TextInputLayout
    private lateinit var passwordSuccessImage: ImageView
    private lateinit var passwordErrorImage: ImageView

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

        setPasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            uiObserver?.setOnCheckedChangeListener(isChecked)
            if(isChecked){
                editTextPasswordLayout.visibility = View.VISIBLE
                switchOffMessage.visibility = View.GONE
                setPassphraseText.visibility = View.VISIBLE
            }
            else{
                if(!btnSend.isEnabled) enableSendEmailButton()
                editTextPasswordLayout.visibility = View.GONE
                switchOffMessage.visibility = View.VISIBLE
                setPassphraseText.visibility = View.GONE
            }
        }


        return nonCriptextEmailSendDialog
    }

    private fun initializeLayoutComponents(){
        setPasswordSwitch = view.findViewById(R.id.set_password_switch)

        editTextPasswordLayout = view.findViewById(R.id.edit_text_password_layout)

        switchOffMessage = view.findViewById(R.id.switch_off_message)
        setPassphraseText = view.findViewById(R.id.set_passphrase_text)

        password = view.findViewById(R.id.password)
        passwordInput = view.findViewById(R.id.password_input)
        passwordSuccessImage = view.findViewById(R.id.success_password)
        passwordErrorImage = view.findViewById(R.id.error_password)

        btnSend = (view.findViewById(R.id.non_criptext_email_send) as Button)
        btnCancel = (view.findViewById(R.id.non_criptext_email_cancel) as Button)
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



    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                                   observer: ComposerUIObserver?) {

        btnSend.setOnClickListener {
                uiObserver?.sendDialogButtonPressed()
                dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            uiObserver?.sendDialogCancelPressed()
            dialog.dismiss()
        }
    }
}