package com.criptext.mail.scenes.composer

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.webkit.WebView
import android.widget.*
import com.criptext.mail.db.models.Contact
import com.criptext.mail.R
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.scenes.composer.ui.*
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewObserver
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.squareup.picasso.Picasso
import com.tokenautocomplete.TokenCompleteTextView
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.toolbar.AztecToolbar

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    var attachmentsObserver: AttachmentViewObserver?
    fun bindWithModel(firstTime: Boolean,
                      composerInputData: ComposerInputData,
                      attachments: ArrayList<ComposerAttachment>,
                      signature: String)
    fun setFocusToComposer()
    fun getDataInputByUser(): ComposerInputData
    fun showError(message: UIMessage)
    fun setContactSuggestionList(contacts: Array<Contact>)
    fun toggleExtraFieldsVisibility(visible: Boolean)
    fun showAttachmentErrorDialog(filename: String)
    fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener)
    fun showNonCriptextEmailSendDialog(observer: ComposerUIObserver?)
    fun notifyAttachmentSetChanged()
    fun disableSendButtonOnDialog()
    fun enableSendButtonOnDialog()
    fun setPasswordError(message: UIMessage?)
    fun togglePasswordSuccess(show: Boolean)
    fun setPasswordForNonCriptextFromDialog(password: String?)

    class Default(view: View, private val keyboard: KeyboardManager): ComposerScene {

        private val ctx = view.context
        private val nonCriptextEmailSendDialog = NonCriptextEmailSendDialog(ctx)

        private var passwordForNonCriptextUsersFromDialog: String? = null

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
            HTMLEditText(
                    visualEditor = view.findViewById<AztecText>(INPUT_BODY_ID),
                    sourceEditor = null,
                    toolbar = view.findViewById<AztecToolbar>(R.id.formatting_toolbar),
                    hint = ctx.resources.getString(R.string.message))
        })
        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.backButton) as ImageView
        }
        private val imageViewArrow: ImageView by lazy {
            view.findViewById<ImageView>(R.id.imageViewArrow) as ImageView
        }
        private val responseBody: WebView by lazy {
            view.findViewById<WebView>(R.id.responseBody) as WebView
        }
        private val imageViewMore: ImageView by lazy {
            view.findViewById<ImageView>(R.id.imageViewMore) as ImageView
        }
        private val attachmentButton: View by lazy {
            view.findViewById<View>(R.id.attachment_button)
        }
        private val attachmentRecyclerView: RecyclerView by lazy {
            view.findViewById<RecyclerView>(R.id.composer_attachment_recyclerview)
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

        override var attachmentsObserver: AttachmentViewObserver? = object : AttachmentViewObserver {
            override fun onAttachmentViewClick(position: Int) {}

            override fun onRemoveAttachmentClicked(position: Int) {
                observer?.onAttachmentRemoveClicked(position)
            }
        }


        override fun bindWithModel(firstTime: Boolean,
                                   composerInputData: ComposerInputData,
                                   attachments: ArrayList<ComposerAttachment>,
                                   signature: String) {

            val mLayoutManager = LinearLayoutManager(ctx)
            val adapter = AttachmentListAdapter(ctx, attachments)
            adapter.observer = attachmentsObserver
            attachmentRecyclerView.layoutManager = mLayoutManager
            attachmentRecyclerView.adapter = adapter

            subjectEditText.setText(composerInputData.subject, TextView.BufferType.NORMAL)
            bodyEditText.text = if(composerInputData.body.isEmpty())
                MailBody.createNewTemplateMessageBody(composerInputData.body, signature)
            else composerInputData.body
            bodyEditText.setMinHeight()

            setupAutoCompletion(firstTime = firstTime,
                    toContacts = composerInputData.to,
                    ccContacts = composerInputData.cc, bccContacts = composerInputData.bcc)

            setListeners()
        }

        override fun notifyAttachmentSetChanged() {
            attachmentRecyclerView.adapter.notifyDataSetChanged()
        }

        private fun setListeners() {
            subjectEditText.onFocusChangeListener = onEditTextGotFocus
            bodyEditText.onFocusChangeListener = onEditTextGotFocus

            backButton.setOnClickListener {
                observer?.onBackButtonClicked()
            }

            imageViewArrow.setOnClickListener {
                toggleExtraFieldsVisibility(ccInput.visibility == View.GONE)
            }

            imageViewMore.setOnClickListener {
                responseBody.visibility = if(responseBody.visibility == View.VISIBLE) View.GONE
                                            else View.VISIBLE
            }

            attachmentButton.setOnClickListener {
                observer?.onAttachmentButtonClicked()
            }
        }

        override fun setFocusToComposer() {
            bodyEditText.setFocus()
        }

        override fun getDataInputByUser(): ComposerInputData {
            return ComposerInputData(to = toInput.objects, cc = ccInput.objects,
                    bcc = bccInput.objects, subject = subjectEditText.text.toString(),
                    body = bodyEditText.text, passwordForNonCriptextUsers = passwordForNonCriptextUsersFromDialog)
        }

        override fun showError(message: UIMessage) {
            Toast.makeText(ctx, ctx.getLocalizedUIMessage(message), Toast.LENGTH_SHORT).show()
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

        override fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener) {
            val builder = AlertDialog.Builder(ctx)
            builder.setMessage(ctx.resources.getString(R.string.you_wanna_save))
                    .setPositiveButton(ctx.resources.getString(R.string.save), dialogClickListener)
                    .setNegativeButton(ctx.resources.getString(R.string.discard), dialogClickListener).show()
        }

        override fun showNonCriptextEmailSendDialog(observer: ComposerUIObserver?) {
            nonCriptextEmailSendDialog.showNonCriptextEmailSendDialog(observer)
        }

        override fun showAttachmentErrorDialog(filename: String){
            val builder = AlertDialog.Builder(ctx)
            builder.setMessage(ctx.resources.getString(R.string.unable_to_upload, filename))
                    .show()
        }

        private fun setupAutoCompletion(firstTime: Boolean, toContacts: List<Contact>,
                                        ccContacts: List<Contact>, bccContacts: List<Contact>) {

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
                fillRecipients(toContacts, bccContacts, ccContacts)
            }

            val splitChar = charArrayOf(',', ';', ' ')

            toInput.setSplitChar(splitChar)
            ccInput.setSplitChar(splitChar)
            bccInput.setSplitChar(splitChar)
        }

        private fun fillRecipients(toContacts: List<Contact>,
                                   bccContacts: List<Contact>,
                                   ccContacts: List<Contact>) {
            for (contact in toContacts) {
                toInput.addObject(contact)
            }
            for (contact in bccContacts) {
                bccInput.addObject(contact)
            }
            for (contact in ccContacts) {
                ccInput.addObject(contact)
            }
        }

        override fun disableSendButtonOnDialog() {
            nonCriptextEmailSendDialog.disableSendEmailButton()
        }

        override fun enableSendButtonOnDialog() {
            nonCriptextEmailSendDialog.enableSendEmailButton()
        }

        override fun setPasswordError(message: UIMessage?) {
            nonCriptextEmailSendDialog.setPasswordError(message)
        }

        override fun togglePasswordSuccess(show: Boolean) {
            nonCriptextEmailSendDialog.togglePasswordSuccess(show)
        }

        override fun setPasswordForNonCriptextFromDialog(password: String?) {
            passwordForNonCriptextUsersFromDialog = password
        }
    }



    companion object {
        val INPUT_TO_ID = R.id.input_to
        val INPUT_SUBJECT_ID = R.id.subject_input
        val INPUT_BODY_ID = R.id.body_input
    }
}
