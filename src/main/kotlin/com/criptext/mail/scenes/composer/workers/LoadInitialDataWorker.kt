package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result

/**
 * Created by gabriel on 7/2/18.
 */
class LoadInitialDataWorker(
        httpClient: HttpClient,
        private val db: ComposerLocalDB,
        private val activeAccount: ActiveAccount,
        override val publishFn: (ComposerResult.LoadInitialData) -> Unit,
        private val userEmailAddress: String,
        private val signature: String,
        private val composerType: ComposerType,
        private val emailId: Long)
    : BackgroundWorker<ComposerResult.LoadInitialData> {
    override val canBeParallelized = true

    private val apiClient = ComposerAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): ComposerResult.LoadInitialData {
        val message = UIMessage(R.string.composer_load_error)
        return ComposerResult.LoadInitialData.Failure(message)
    }

    private fun convertDraftToInputData(fullEmail: FullEmail): ComposerInputData {
        val attachments = ArrayList(fullEmail.files.map {
            ComposerAttachment(0, it.name, 100,
                    it.token, FileUtils.getAttachmentTypeFromPath(it.name),
                    it.size, it.fileKey, it.cid)
        })
        var to = fullEmail.to
        var cc = fullEmail.cc
        var bcc = fullEmail.bcc
        val allRecipients = (to + cc + bcc).map { EmailAddressUtils.extractEmailAddressDomain(it.email) }
                .filter { domain -> domain !in (ContactDomainCheckData.KNOWN_EXTERNAL_DOMAINS
                        .map { it.name } + activeAccount.domain) }
        val operation = Result.of { apiClient.getIsSecureDomain(allRecipients) }

        if(operation is Result.Success){
            val data = ContactDomainCheckData.fromJSON(operation.value.body)
            to = getCriptextContacts(to, data)
            cc = getCriptextContacts(cc, data)
            bcc = getCriptextContacts(bcc, data)
        }
        return ComposerInputData(to = to, cc = cc, bcc = bcc,
                body = fullEmail.email.content, subject = fullEmail.email.subject,
                attachments = attachments, fileKey = fullEmail.fileKey)
    }

    private fun convertReplyToInputData(fullEmail: FullEmail, replyToAll: Boolean): ComposerInputData {
        val replyTo = fullEmail.email.replyTo
        val replyToCOntact = if(replyTo == null) null
        else
            Contact(id = 0, name = EmailAddressUtils.extractName(replyTo), email = EmailAddressUtils.extractEmailAddress(replyTo),
                    isTrusted = false, score = 0, spamScore = 0)
        var to = if (replyToAll) {
            if(fullEmail.from.email == userEmailAddress)
                fullEmail.to
            else
                fullEmail.to.filter { it.email != userEmailAddress }
                    .plus(replyToCOntact ?: fullEmail.from)
        }
        else {
            if(fullEmail.from.email == userEmailAddress)
                fullEmail.to
            else
                listOf(replyToCOntact ?: fullEmail.from)
        }

        val template = if(replyToAll) (composerType as ComposerType.ReplyAll).template
        else
            (composerType as ComposerType.Reply).template

        var cc = if (replyToAll) fullEmail.cc.filter { it.email != userEmailAddress } else emptyList()

        val allRecipients = (to + cc).map { EmailAddressUtils.extractEmailAddressDomain(it.email) }
                .filter { domain -> domain !in (ContactDomainCheckData.KNOWN_EXTERNAL_DOMAINS
                        .map { it.name } + activeAccount.domain) }

        val operation = Result.of { apiClient.getIsSecureDomain(allRecipients) }

        if(operation is Result.Success){
            val data = ContactDomainCheckData.fromJSON(operation.value.body)
            to = getCriptextContacts(to, data)
            cc = getCriptextContacts(cc, data)
        }

        val subject = (if(fullEmail.email.subject.matches("^(Re|RE): .*\$".toRegex())) "" else "RE: ") +
                                fullEmail.email.subject
        val body = MailBody.createNewReplyMessageBody(
                originMessageHtml = fullEmail.email.content,
                date = System.currentTimeMillis(),
                senderName = fullEmail.from.name,
                signature = signature,
                template = template)
        return ComposerInputData(to = to, cc = cc, bcc = emptyList(),
                body = body, subject = subject, attachments = null, fileKey = null)
    }

    private fun convertForwardToInputData(fullEmail: FullEmail): ComposerInputData {
        val body = MailBody.createNewForwardMessageBody(
                fullEmail = fullEmail,
                template = (composerType as ComposerType.Forward).template,
                signature = signature)
        val subject = (if(fullEmail.email.subject.matches("^(Fw|FW|Fwd|FWD): .*\$".toRegex())) "" else "FW: ") +
                fullEmail.email.subject
        val attachments = ArrayList<ComposerAttachment>(fullEmail.files.map {
            ComposerAttachment(0, it.name, 100,
                    it.token, FileUtils.getAttachmentTypeFromPath(it.name),
                    it.size, it.fileKey, it.cid)
        })

        return ComposerInputData(to = emptyList(), cc = emptyList(), bcc = emptyList(),
                body = body, subject = subject, attachments = if (attachments.isEmpty()) null
        else attachments, fileKey = fullEmail.fileKey)

    }

    private fun createSupportInputData(): ComposerInputData {
        val supportTemplate = (composerType as ComposerType.Support).template
        val supportContact= db.contactDao.getContact(supportTemplate.contact, activeAccount.id)

        return if(supportContact != null){
            ComposerInputData(to = listOf(supportContact), cc = emptyList(), bcc = emptyList(),
                    body = supportTemplate.body, subject = supportTemplate.subject,
                    attachments = null, fileKey = null)
        }else{
            val newSupportContact = Contact(id = 0, email = "support@${Contact.mainDomain}",
                    name = "Criptext Support", isTrusted = true, score = 0, spamScore = 0)
            db.contactDao.insertAll(listOf(newSupportContact))
            ComposerInputData(to = listOf(newSupportContact), cc = emptyList(), bcc = emptyList(),
                    body = supportTemplate.body, subject = supportTemplate.subject,
                    attachments = null, fileKey = null)
        }
    }

    private fun createMailToInputData(to: String): ComposerInputData {
        val contact = db.contactDao.getContact(to, activeAccount.id)

        return if(contact != null){
            ComposerInputData(to = listOf(contact), cc = emptyList(), bcc = emptyList(),
                    body = "", subject = "", fileKey = null, attachments = null)
        }else{
            val newContact = Contact(id = 0, email = to,
                    name = EmailAddressUtils.extractName(to), isTrusted = false, score = 0, spamScore = 0)
            db.contactDao.insertAll(listOf(newContact))
            ComposerInputData(to = listOf(newContact), cc = emptyList(), bcc = emptyList(),
                    body = "", subject = "", attachments = null, fileKey = null)
        }
    }

    private fun createComposerInputData(loadedEmail: FullEmail): ComposerInputData =
        when (composerType) {
                is ComposerType.Forward -> convertForwardToInputData(loadedEmail)
                is ComposerType.Reply -> convertReplyToInputData(loadedEmail, replyToAll = false)
                is ComposerType.ReplyAll -> convertReplyToInputData(loadedEmail, replyToAll = true)
                else -> convertDraftToInputData(loadedEmail)
            }

    private fun getCriptextContacts(contacts: List<Contact>, checkedData: List<ContactDomainCheckData>): List<Contact> {
        val isCriptext = contacts.map { it.email }
                .filter { email ->
                    EmailAddressUtils.extractEmailAddressDomain(email) in
                            checkedData.filter { it.isCriptextDomain }
                                    .map { it.name } }
        contacts.forEachIndexed { _, contact ->
            if(contact.email in isCriptext)
                contact.isCriptextDomain = true
        }
        return contacts
    }

    override fun work(reporter: ProgressReporter<ComposerResult.LoadInitialData>): ComposerResult.LoadInitialData? {
        val loadedEmail = db.loadFullEmail(emailId, activeAccount)
        return if (loadedEmail != null) {
            val composerInputData = createComposerInputData(loadedEmail)
            ComposerResult.LoadInitialData.Success(composerInputData)
        } else {
            when(composerType){
                is ComposerType.Support -> ComposerResult.LoadInitialData.Success(createSupportInputData())
                is ComposerType.MailTo -> ComposerResult.LoadInitialData.Success(createMailToInputData(composerType.to))
                else -> {
                    val message = UIMessage(R.string.composer_load_error)
                    ComposerResult.LoadInitialData.Failure(message)
                }
            }
        }
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
