package com.criptext.mail.scenes.settings.change_email

import android.annotation.SuppressLint
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.change_email.holders.FormInputViewHolder
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.validation.FormInputState

interface ChangeEmailScene{

    fun attachView(observer: ChangeEmailUIObserver,
                   keyboardManager: KeyboardManager, model: ChangeEmailModel)
    fun showMessage(message: UIMessage)
    fun showEnterPasswordDialog()
    fun enableChangeButton()
    fun disableChangeButton()
    fun dialogToggleLoad(loading: Boolean)
    fun enterPasswordDialogError(message: UIMessage)
    fun enterPasswordDialogDismiss()
    fun setRecoveryEmailState(state: FormInputState)

    class Default(val view: View): ChangeEmailScene{
        private lateinit var changeEmailUIObserver: ChangeEmailUIObserver

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewCurrentEmail: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.input)
        }

        private val changeButton: Button by lazy {
            view.findViewById<Button>(R.id.change_button)
        }

        private val textViewCurrentEmailInput: FormInputViewHolder by lazy {
            FormInputViewHolder(
                    textInputLayout = view.findViewById(R.id.input_layout),
                    editText = textViewCurrentEmail,
                    validView = view.findViewById(R.id.success),
                    errorView = view.findViewById(R.id.error),
                    disableSubmitButton = { -> changeButton.isEnabled = false })
        }

        private val enterPasswordDialog = EnterPasswordDialog(context)

        override fun attachView(observer: ChangeEmailUIObserver, keyboardManager: KeyboardManager,
                                model: ChangeEmailModel) {
            changeEmailUIObserver = observer
            backButton.setOnClickListener {
                changeEmailUIObserver.onBackButtonPressed()
            }

            changeButton.setOnClickListener{
                changeEmailUIObserver.onChangeButtonPressed(textViewCurrentEmail.text.toString())
            }

            textViewCurrentEmail.text = SpannableStringBuilder(model.recoveryEmail)
            recoveryEmailTextListener()
        }

        private fun recoveryEmailTextListener() {
            textViewCurrentEmail.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    changeEmailUIObserver.onRecoveryEmailTextChanged(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        override fun setRecoveryEmailState(state: FormInputState) {
            textViewCurrentEmailInput.setState(state)
        }

        override fun showEnterPasswordDialog() {
            enterPasswordDialog.showDialog(changeEmailUIObserver)
        }

        override fun disableChangeButton() {
            changeButton.isEnabled = false
        }

        override fun enableChangeButton() {
            changeButton.isEnabled = true
        }

        override fun dialogToggleLoad(loading: Boolean) {
            enterPasswordDialog.toggleLoad(loading)
        }

        override fun enterPasswordDialogError(message: UIMessage) {
            enterPasswordDialog.setPasswordError(message)
        }

        override fun enterPasswordDialogDismiss() {
            enterPasswordDialog.dismissDialog()
        }

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    context,
                    context.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }

}