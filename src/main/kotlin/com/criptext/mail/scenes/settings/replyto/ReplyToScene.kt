package com.criptext.mail.scenes.settings.replyto

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import com.criptext.mail.R
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.google.android.material.textfield.TextInputLayout

interface ReplyToScene{

    fun attachView(replyToUIObserver: ReplyToUIObserver, signature: String,
                   keyboardManager: KeyboardManager)
    fun showMessage(message: UIMessage)
    fun setEmailError(message: UIMessage?)
    fun enableSaveButton()
    fun disableSaveButton()
    fun clearTextBox()

    class Default(val view: View): ReplyToScene{

        private val context = view.context

        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        private val textViewStatus: TextView by lazy {
            view.findViewById<TextView>(R.id.textview_status)
        }

        private val swicthStatus: Switch by lazy {
            view.findViewById<Switch>(R.id.switch_status)
        }

        private val viewSignature: View by lazy{
            view.findViewById<View>(R.id.view_signature)
        }

        private val bodyEditText: AppCompatEditText by lazy {
            view.findViewById<AppCompatEditText>(R.id.input)
        }

        private val editTextEmailLayout: TextInputLayout by lazy {
            view.findViewById<TextInputLayout>(R.id.input_layout)
        }

        private val changeEmailButton: Button by lazy {
            view.findViewById<Button>(R.id.change_email_button)
        }

        override fun attachView(replyToUIObserver: ReplyToUIObserver, replyToEmail: String,
                                keyboardManager: KeyboardManager) {

            backButton.setOnClickListener {
                replyToUIObserver.onBackButtonPressed()
            }

            changeEmailButton.setOnClickListener {
                replyToUIObserver.onRecoveryChangeButonPressed()
            }

            textListener(replyToUIObserver)

            displayReplyTo(replyToEmail, keyboardManager, replyToUIObserver)
        }

        private fun hidePasswordError() {
            editTextEmailLayout.error = ""
        }

        private fun showPasswordError(message: UIMessage) {
            editTextEmailLayout.error = context.getLocalizedUIMessage(message)
        }

        override fun setEmailError(message: UIMessage?) {
            if (message == null) {
                hidePasswordError()
                enableSaveButton()
            } else {
                showPasswordError(message)
                disableSaveButton()
            }
        }

        override fun disableSaveButton() {
            changeEmailButton.isEnabled = false
        }

        override fun enableSaveButton() {
            changeEmailButton.isEnabled = true
        }

        private fun textListener(uiObserver: ReplyToUIObserver) {
            bodyEditText.addTextChangedListener( object : TextWatcher {
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    uiObserver.onRecoveryEmailChanged(text.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
        }

        override fun clearTextBox() {
            bodyEditText.setText("")
        }

        private fun displayReplyTo(replyToEmail: String, keyboardManager: KeyboardManager, uiObserver: ReplyToUIObserver) {
            bodyEditText.setText(replyToEmail)
            if(replyToEmail.isNotEmpty()){
                swicthStatus.isChecked = true
                textViewStatus.text = context.resources.getString(R.string.on)
                viewSignature.visibility = View.VISIBLE
            }

            swicthStatus.setOnCheckedChangeListener { _, isChecked ->
                viewSignature.visibility = if(isChecked) View.VISIBLE else View.GONE
                textViewStatus.text = if(isChecked) context.resources.getString(R.string.on)
                else context.resources.getString(R.string.off)
                if(isChecked){
                    keyboardManager.showKeyboard(bodyEditText)
                }
                else{
                    uiObserver.onTurnOffReplyTo()
                    keyboardManager.hideKeyboard()
                }
            }
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