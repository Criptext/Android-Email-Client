package com.criptext.mail.scenes.composer

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.MailBody
import com.criptext.mail.scenes.composer.ui.*
import com.criptext.mail.scenes.composer.ui.holders.AttachmentViewObserver
import com.criptext.mail.utils.EmailUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.AccountSuspendedDialog
import com.criptext.mail.utils.ui.ConfirmPasswordDialog
import com.criptext.mail.utils.ui.LinkNewDeviceAlertDialog
import com.criptext.mail.utils.ui.MessageAndProgressDialog
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.squareup.picasso.Picasso
import com.tokenautocomplete.TokenCompleteTextView

/**
 * Created by gabriel on 2/26/18.
 */

interface ComposerScene {
    var observer: ComposerUIObserver?
    var attachmentsObserver: AttachmentViewObserver?
    fun showMessage(message: UIMessage)
    fun bindWithModel(firstTime: Boolean,
                      composerInputData: ComposerInputData,
                      attachments: ArrayList<ComposerAttachment>,
                      signature: String)
    fun setFocusToComposer()
    fun setFocusToSubject()
    fun setFocusToTo()
    fun getDataInputByUser(): ComposerInputData
    fun showError(message: UIMessage)
    fun setContactSuggestionList(contacts: List<Contact>)
    fun toggleExtraFieldsVisibility(visible: Boolean)
    fun showAttachmentErrorDialog(message: UIMessage)
    fun showPayloadTooLargeDialog(filename: String, maxsize: Long)
    fun showMaxFilesExceedsDialog()
    fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener)
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType)
    fun showAttachmentsBottomDialog(observer: ComposerUIObserver?)
    fun showPreparingFileDialog()
    fun dismissPreparingFileDialog()
    fun notifyAttachmentSetChanged()
    fun dismissConfirmPasswordDialog()
    fun dismissAccountSuspendedDialog()
    fun showStayInComposerDialog(observer: ComposerUIObserver)
    fun setConfirmPasswordError(message: UIMessage)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun showStartGuideAttachments()
    fun fillFromOptions(list: List<String>)
    fun switchToSimpleFrom(from: String)
    fun contactsInputUpdate(to: List<Contact>, ccContacts: List<Contact>, bccContacts: List<Contact>)

    class Default(view: View, private val keyboard: KeyboardManager): ComposerScene {

        private val ctx = view.context
        private val attachmentBottomDialog = AttachmentsBottomDialog(ctx)
        private val confirmPassword = ConfirmPasswordDialog(ctx)
        private val accountSuspended = AccountSuspendedDialog(ctx)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(ctx)
        private val preparingFileDialog = MessageAndProgressDialog(ctx, UIMessage(R.string.preparing_file))
        private val stayInComposerDialog = StayInComposerDialog(ctx)

        private val fromAddresses: Spinner by lazy {
            view.findViewById<Spinner>(R.id.spinner_from)
        }

        private val fromAddress: TextView by lazy {
            view.findViewById<TextView>(R.id.from_address)
        }

        private val toInput: ContactCompletionView by lazy {
            view.findViewById<ContactCompletionView>(INPUT_TO_ID)
        }
        private val ccInput: ContactCompletionView by lazy {
            view.findViewById<ContactCompletionView>(R.id.input_cc)
        }
        private val bccInput: ContactCompletionView by lazy {
            view.findViewById<ContactCompletionView>(R.id.input_bcc)
        }
        private val subjectEditText: EditText by lazy {
            view.findViewById<EditText>(INPUT_SUBJECT_ID)
        }
        private val bodyEditText: HTMLEditText by lazy {
            HTMLEditText(
                    richEditor = view.findViewById(INPUT_BODY_ID),
                    hint = ctx.resources.getString(R.string.message))
        }
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
                observer?.onRecipientAdded()
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
            attachmentRecyclerView.adapter?.notifyDataSetChanged()
        }

        private fun setListeners() {
            subjectEditText.onFocusChangeListener = onEditTextGotFocus

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

        override fun setFocusToTo() {
            toInput.requestFocus()
            keyboard.showKeyboardWithDelay(toInput)
        }

        override fun setFocusToComposer() {
            bodyEditText.setFocus()
            keyboard.showKeyboardWithDelay(bodyEditText.view)
        }

        override fun setFocusToSubject() {
            subjectEditText.requestFocus()
            keyboard.showKeyboardWithDelay(subjectEditText)
        }

        override fun getDataInputByUser(): ComposerInputData {
            return ComposerInputData(to = toInput.objects, cc = ccInput.objects,
                    bcc = bccInput.objects, subject = subjectEditText.text.toString(),
                    body = bodyEditText.text,
                    attachments = null, fileKey = null)
        }

        override fun showError(message: UIMessage) {
            Toast.makeText(ctx, ctx.getLocalizedUIMessage(message), Toast.LENGTH_SHORT).show()
        }

        override fun setContactSuggestionList(contacts: List<Contact>) {
            val adapter = ContactsFilterAdapter(ctx, contacts)
            toInput.setAdapter(adapter)
            ccInput.setAdapter(adapter)
            bccInput.setAdapter(adapter)
        }

        override fun toggleExtraFieldsVisibility(visible: Boolean) {
            val visibility = if (visible) View.VISIBLE else View.GONE
            ccInput.visibility = visibility
            bccInput.visibility = visibility
            Picasso.get().load(
                    if(visible) R.drawable.arrowup else
                    R.drawable.arrowdown).into(imageViewArrow)
        }

        override fun fillFromOptions(list: List<String>) {
            val adapter = ArrayAdapter<String>(
                    ctx, R.layout.support_simple_spinner_dropdown_item, list
            )
            adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            fromAddresses.adapter = adapter
            fromAddresses.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    observer?.onSenderSelectedItem(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }

        override fun switchToSimpleFrom(from: String) {
            fromAddresses.visibility = View.GONE
            fromAddress.visibility = View.VISIBLE
            fromAddress.text = from
        }

        override fun showStartGuideAttachments() {
            observer?.showStartGuideAttachments(attachmentButton)
        }

        override fun showDraftDialog(dialogClickListener: DialogInterface.OnClickListener) {
            val builder = AlertDialog.Builder(ctx)
            builder.setMessage(ctx.resources.getString(R.string.you_wanna_save))
                    .setPositiveButton(ctx.resources.getString(R.string.save), dialogClickListener)
                    .setNegativeButton(ctx.resources.getString(R.string.discard), dialogClickListener).show()
        }

        override fun showAttachmentsBottomDialog(observer: ComposerUIObserver?) {
            attachmentBottomDialog.showAttachmentsDialog(observer)
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun showAccountSuspendedDialog(observer: UIObserver, email: String, dialogType: DialogType) {
            accountSuspended.showDialog(observer, email, dialogType)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun dismissAccountSuspendedDialog() {
            accountSuspended.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
        }

        override fun showAttachmentErrorDialog(message: UIMessage){
            val builder = AlertDialog.Builder(ctx)
            builder.setMessage(ctx.getLocalizedUIMessage(message))
                    .show()
        }

        override fun showPayloadTooLargeDialog(filename: String, maxsize: Long){
            val fullName = FileUtils.getName(filename)
            val builder = AlertDialog.Builder(ctx)
            val size = FileUtils.readableFileSize(maxsize, 1000)
            builder.setTitle(ctx.resources.getString(R.string.error_attach_file))
                    .setMessage(ctx.resources.getString(R.string.payload_too_large, fullName, size))
                    .show()
        }

        override fun showMaxFilesExceedsDialog(){
            val builder = AlertDialog.Builder(ctx)
            val size = FileUtils.readableFileSize(EmailUtils.ATTACHMENT_SIZE_LIMIT.toLong(), 1000)
            builder.setTitle(ctx.resources.getString(R.string.error_email_max_files_title))
                    .setMessage(ctx.resources.getString(R.string.error_email_max_size, size))
                    .show()
        }

        override fun showPreparingFileDialog() {
            preparingFileDialog.showDialog()
        }

        override fun dismissPreparingFileDialog() {
            preparingFileDialog.dismiss()
        }

        override fun showStayInComposerDialog(observer: ComposerUIObserver) {
            stayInComposerDialog.showLinkDeviceAuthDialog(observer)
        }

        override fun contactsInputUpdate(to: List<Contact>, ccContacts: List<Contact>, bccContacts: List<Contact>) {
            toInput.setTokenListener(null)
            ccInput.setTokenListener(null)
            bccInput.setTokenListener(null)
            toInput.clear()
            ccInput.clear()
            bccInput.clear()
            fillRecipients(to, ccContacts, bccContacts)
            (toInput.adapter as ContactsFilterAdapter).updateIsCriptextDomain(to)
            (ccInput.adapter as ContactsFilterAdapter).updateIsCriptextDomain(ccContacts)
            (bccInput.adapter as ContactsFilterAdapter).updateIsCriptextDomain(bccContacts)
            toInput.setTokenListener(onTokenChanged)
            ccInput.setTokenListener(onTokenChanged)
            bccInput.setTokenListener(onTokenChanged)
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
                toInput.setPrefix(ctx.getLocalizedUIMessage(UIMessage(R.string.to_popup)),ContextCompat.getColor(toInput.context, R.color.inputHint))
                ccInput.setPrefix(ctx.getLocalizedUIMessage(UIMessage(R.string.cc_popup)),ContextCompat.getColor(toInput.context, R.color.inputHint))
                bccInput.setPrefix(ctx.getLocalizedUIMessage(UIMessage(R.string.bcc_popup)),ContextCompat.getColor(toInput.context, R.color.inputHint))
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

        override fun showMessage(message: UIMessage) {
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(
                    ctx,
                    ctx.getLocalizedUIMessage(message),
                    duration)
            toast.show()
        }
    }



    companion object {
        val INPUT_TO_ID = R.id.input_to
        val INPUT_SUBJECT_ID = R.id.subject_input
        val INPUT_BODY_ID = R.id.body_input
    }
}
