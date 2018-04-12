package com.email.scenes.composer

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.email.db.models.Contact
import com.email.R
import com.email.scenes.composer.ui.ComposerUIObserver
import com.email.scenes.composer.ui.ContactCompletionView
import com.email.scenes.composer.ui.ContactsFilterAdapter
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.composer.ui.HTMLEditText
import com.email.utils.UIMessage
import com.squareup.picasso.Picasso
import com.tokenautocomplete.TokenCompleteTextView
import jp.wasabeef.richeditor.RichEditor

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>, composerInputData: ComposerInputData)
    fun getDataInputByUser(): ComposerInputData
    fun showError(message: UIMessage)
    fun setContactSuggestionList(contacts: Array<Contact>)
    fun toggleExtraFieldsVisibility(visible: Boolean)

    class Default(view: View): ComposerScene {

        private val ctx = view.context

        private val toInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(INPUT_TO_ID)
        })
        private val ccInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(R.id.input_cc)
        })
        private val bccInput: ContactCompletionView by lazy({
            view.findViewById<ContactCompletionView>(R.id.input_bcc)
        })
        private val subjectEditText: EditText by lazy({
            view.findViewById<EditText>(INPUT_SUBJECT_ID)
        })
        private val bodyEditText: HTMLEditText by lazy({
            HTMLEditText(view.findViewById<RichEditor>(INPUT_BODY_ID))
        })
        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.backButton) as ImageView
        }
        private val imageViewArrow: ImageView by lazy {
            view.findViewById<ImageView>(R.id.imageViewArrow) as ImageView
        }

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

        override fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>, uiData: ComposerInputData) {
            bodyEditText.text = uiData.body
            uiData.to.forEach { contact ->
                toInput.addObject(contact)
            }

            uiData.cc.forEach { contact ->
                ccInput.addObject(contact)
            }

            uiData.bcc.forEach { contact ->
                bccInput.addObject(contact)
            }

            setupAutoCompletion(firstTime = firstTime, defaultRecipients = defaultRecipients,
                    toContacts = uiData.to, ccContacts = uiData.cc, bccContacts = uiData.bcc)

            subjectEditText.onFocusChangeListener = onEditTextGotFocus
            bodyEditText.onFocusChangeListener = onEditTextGotFocus

            val splitChar = charArrayOf(',', ';', ' ')
            toInput.setSplitChar(splitChar)
            ccInput.setSplitChar(splitChar)
            bccInput.setSplitChar(splitChar)

            backButton.setOnClickListener {
                observer?.onBackButtonClicked()
            }

            imageViewArrow.setOnClickListener {
                toggleExtraFieldsVisibility(ccInput.visibility == View.GONE)
            }
        }

        override fun getDataInputByUser(): ComposerInputData {
            return ComposerInputData(to = toInput.objects, cc = ccInput.objects, bcc = bccInput.objects,
                    subject = subjectEditText.text.toString(), body = bodyEditText.text)
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
            Picasso.with(imageViewArrow.context).load(
                    if(visible) R.drawable.arrow_up else
                    R.drawable.arrow_down).into(imageViewArrow)
        }

        private fun setupAutoCompletion(firstTime: Boolean, defaultRecipients: List<Contact>,
                                        toContacts: List<Contact>, ccContacts: List<Contact>,
                                        bccContacts: List<Contact>) {
            toInput.allowDuplicates(false)
            toInput.setTokenListener(onTokenChanged)
            ccInput.allowDuplicates(false)
            ccInput.setTokenListener(onTokenChanged)
            bccInput.allowDuplicates(false)
            bccInput.setTokenListener(onTokenChanged)

            if (firstTime) {
                toInput.setPrefix("To:   ",ContextCompat.getColor(toInput.context, R.color.inputHint))
                ccInput.setPrefix("Cc:   ",ContextCompat.getColor(toInput.context, R.color.inputHint))
                bccInput.setPrefix("Bcc: ",ContextCompat.getColor(toInput.context, R.color.inputHint))
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

    companion object {
        val INPUT_TO_ID = R.id.input_to
        val INPUT_SUBJECT_ID = R.id.subject_input
        val INPUT_BODY_ID = R.id.body_input
    }
}