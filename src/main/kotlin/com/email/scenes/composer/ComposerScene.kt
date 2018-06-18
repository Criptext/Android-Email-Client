package com.email.scenes.composer

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.webkit.WebView
import android.widget.*
import com.email.db.models.Contact
import com.email.R
import com.email.scenes.composer.data.*
import com.email.scenes.composer.ui.*
import com.email.scenes.composer.ui.holders.AttachmentViewObserver
import com.email.utils.HTMLUtils
import com.email.utils.KeyboardManager
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage
import com.squareup.picasso.Picasso
import com.tokenautocomplete.TokenCompleteTextView
import jp.wasabeef.richeditor.RichEditor

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    var attachmentsObserver: AttachmentViewObserver?
    fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>,
                      composerInputData: ComposerInputData, replyData: ReplyData?,
                      attachments: LinkedHashMap<String, ComposerAttachment>)
    fun getDataInputByUser(): ComposerInputData
    fun showError(message: UIMessage)
    fun setContactSuggestionList(contacts: Array<Contact>)
    fun toggleExtraFieldsVisibility(visible: Boolean)
    fun showAttachmentErrorDialog(filename: String)
    fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener)
    fun notifyAttachmentSetChanged()

    class Default(view: View, private val keyboard: KeyboardManager): ComposerScene {


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
            HTMLEditText(view.findViewById<RichEditor>(INPUT_BODY_ID), scrollView)
        })
        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.backButton) as ImageView
        }
        private val imageViewArrow: ImageView by lazy {
            view.findViewById<ImageView>(R.id.imageViewArrow) as ImageView
        }
        private val scrollView: ScrollView by lazy {
            view.findViewById<ScrollView>(R.id.scrollViewCompose) as ScrollView
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
            override fun onRemoveAttachmentClicked(position: Int) {
                observer?.onAttachmentRemoveClicked(position)
            }
        }


        override fun bindWithModel(firstTime: Boolean, defaultRecipients: List<Contact>,
                                   composerInputData: ComposerInputData, replyData: ReplyData?,
                                   attachments: LinkedHashMap<String, ComposerAttachment>) {
            val mLayoutManager = LinearLayoutManager(ctx)
            val adapter = AttachmentListAdapter(ctx, attachments)
            adapter.observer = attachmentsObserver
            attachmentRecyclerView.layoutManager = mLayoutManager
            attachmentRecyclerView.adapter = adapter
            subjectEditText.setText(composerInputData.subject, TextView.BufferType.NORMAL)
            when(replyData?.composerType) {
                ComposerTypes.FORWARD -> {
                    bodyEditText.text = MailBody.createNewForwardMessageBody(
                            composerInputData.body, "")
                }
                ComposerTypes.REPLY, ComposerTypes.REPLY_ALL -> {
                    bodyEditText.setFocus()
                    keyboard.showKeyboardWithDelay(bodyEditText.view)
                    bodyEditText.text = MailBody.createNewReplyMessageBody(
                            originMessageHtml = composerInputData.body,
                            date = System.currentTimeMillis(),
                            senderName = replyData.fullEmail.from.name,
                            signature = "")
                }
                ComposerTypes.CONTINUE_DRAFT -> {
                    bodyEditText.setFocus()
                    keyboard.showKeyboardWithDelay(bodyEditText.view)
                    bodyEditText.text = composerInputData.body
                }
                else -> {
                    bodyEditText.setMinHeight()
                }
            }

            setupAutoCompletion(firstTime = firstTime, defaultRecipients = defaultRecipients,
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

        override fun getDataInputByUser(): ComposerInputData {
            return ComposerInputData(to = toInput.objects, cc = ccInput.objects,
                    bcc = bccInput.objects, subject = subjectEditText.text.toString(),
                    body = bodyEditText.text)
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
                    .setPositiveButton(ctx.resources.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(ctx.resources.getString(R.string.discard), dialogClickListener).show()
        }

        override fun showAttachmentErrorDialog(filename: String){
            val builder = AlertDialog.Builder(ctx)
            builder.setMessage("Unable to upload $filename. Please try again")
                    .show()
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
    }

    companion object {
        val INPUT_TO_ID = R.id.input_to
        val INPUT_SUBJECT_ID = R.id.subject_input
        val INPUT_BODY_ID = R.id.body_input
    }
}
