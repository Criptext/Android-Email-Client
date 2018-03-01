package com.email.scenes.composer

import android.text.InputType
import android.view.View
import android.widget.EditText
import com.email.DB.models.Contact
import com.email.R
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.ContactCompletionView
import com.email.scenes.composer.ui.ContactsFilterAdapter
import com.email.scenes.composer.ui.UIData
import com.email.utils.UIMessage
import com.tokenautocomplete.TokenCompleteTextView


/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>,  uiData: UIData)
    fun getDataInputByUser(): UIData
    fun showError(message: UIMessage)
    fun setContactSuggestionList(contacts: Array<Contact>)
    fun toggleExtraFieldsVisibility(visible: Boolean)

    class Default(view: View): ComposerScene {

        private val ctx = view.context

        private val toInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(R.id.input_to)
        })
        private val ccInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(R.id.input_cc)
        })
        private val bccInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(R.id.input_bcc)
        })
        private val subjectEditText: EditText by lazy({
            view.findViewById<EditText>(R.id.subject_input)
        })
        private val bodyEditText: EditText by lazy({
            view.findViewById<EditText>(R.id.body_input)
        })

        private val onTokenChanged = object : TokenCompleteTextView.TokenListener<Contact> {
            override fun onTokenAdded(token: Contact?) {
                observer?.onRecipientListChanged()
            }

            override fun onTokenRemoved(token: Contact?) {
                observer?.onRecipientListChanged()
            }
        }
        private val onEditTextGotFocus = View.OnFocusChangeListener { editText, hasFocus ->
            if (hasFocus)
                observer?.onSelectedEditTextChanged(editText == toInput)
        }
        override var observer: ComposerUIObserver? = null

        override fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>,  uiData: UIData) {
            setupAutoCompletion(firstTime = firstTime, defaultRecipients = defaultRecipients,
                    toContacts = uiData.to, ccContacts = uiData.cc, bccContacts = uiData.bcc)

            subjectEditText.onFocusChangeListener = onEditTextGotFocus
            bodyEditText.onFocusChangeListener = onEditTextGotFocus
            toInput.onFocusChangeListener = onEditTextGotFocus
        }

        override fun getDataInputByUser(): UIData {
            return UIData(to = emptyList(), cc = emptyList(), bcc = emptyList(),
                    subject = "", body = "")
        }

        override fun showError(message: UIMessage) {
        }

        override fun setContactSuggestionList(contacts: Array<Contact>) {
            val adapter = ContactsFilterAdapter(ctx, contacts)
            toInput.setAdapter(adapter)
            ccInput.setAdapter(adapter)
            bccInput.setAdapter(adapter)
        }

        override fun toggleExtraFieldsVisibility(visible: Boolean) {
            val visibility = if (visible) View.VISIBLE else View.GONE
            ccInput.visibility = visibility
            bccInput.visibility = visibility
        }

        private fun setupAutoCompletion(firstTime: Boolean, defaultRecipients: List<Contact>,
                                        toContacts: List<Contact>, ccContacts: List<Contact>,
                                        bccContacts: List<Contact>) {
            toInput.allowDuplicates(false)
            toInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            toInput.setTokenListener(onTokenChanged)
            ccInput.allowDuplicates(false)
            ccInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            ccInput.setTokenListener(onTokenChanged)
            bccInput.allowDuplicates(false)
            bccInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            bccInput.setTokenListener(onTokenChanged)

            if (firstTime) {
                toInput.setPrefix("To: ")
                ccInput.setPrefix("Cc: ")
                bccInput.setPrefix("Bcc: ")
                defaultRecipients.forEach { toInput.addObject(it) }
                fillRecipients(toContacts, bccContacts, ccContacts)
            }
        }

        private fun fillRecipients(initContacts: List<Contact>,
                                   bccContacts: List<Contact>,
                                   ccContacts: List<Contact>) {
            for (contact in initContacts) {
                toInput.addObject(contact)
            }
            for (contact in bccContacts) {
                bccInput.addObject(contact)
            }
            for (contact in ccContacts) {
                ccInput.addObject(contact)
            }
        }
    }

}