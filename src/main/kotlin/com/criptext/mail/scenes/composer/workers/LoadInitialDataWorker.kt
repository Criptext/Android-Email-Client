package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.scenes.composer.data.*
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.file.FileUtils
import java.lang.StringBuilder

/**
 * Created by gabriel on 7/2/18.
 */
class LoadInitialDataWorker(
        private val db: ComposerLocalDB,
        override val publishFn: (ComposerResult.LoadInitialData) -> Unit,
        private val userEmailAddress: String,
        private val signature: String,
        private val composerType: ComposerType,
        private val emailId: Long)
    : BackgroundWorker<ComposerResult.LoadInitialData> {
    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.LoadInitialData {
        val message = UIMessage(R.string.composer_load_error)
        return ComposerResult.LoadInitialData.Failure(message)
    }

    private fun convertDraftToInputData(fullEmail: FullEmail): ComposerInputData {
        val attachments = ArrayList<ComposerAttachment>(fullEmail.files.map {
            ComposerAttachment(0, it.name, 100,
                    it.token, FileUtils.getAttachmentTypeFromPath(it.name),
                    it.size)
        })
        return ComposerInputData(to = fullEmail.to, cc = fullEmail.cc, bcc = fullEmail.bcc,
                body = fullEmail.email.content, subject = fullEmail.email.subject,
                passwordForNonCriptextUsers = null, attachments = attachments, fileKey = fullEmail.fileKey)
    }

    private fun convertReplyToInputData(fullEmail: FullEmail, replyToAll: Boolean): ComposerInputData {
        val to = if (replyToAll) {
            if(fullEmail.from.email == userEmailAddress)
                fullEmail.to
            else
                fullEmail.to.filter { it.email != userEmailAddress }
                    .plus(fullEmail.from)
        }
        else {
            if(fullEmail.from.email == userEmailAddress)
                fullEmail.to
            else
                listOf(fullEmail.from)
        }

        val template = if(replyToAll) (composerType as ComposerType.ReplyAll).template
        else
            (composerType as ComposerType.Reply).template

        val cc = if (replyToAll) fullEmail.cc.filter { it.email != userEmailAddress } else emptyList()

        val subject = (if(fullEmail.email.subject.matches("^(Re|RE): .*\$".toRegex())) "" else "RE: ") +
                                fullEmail.email.subject
        val body = MailBody.createNewReplyMessageBody(
                originMessageHtml = fullEmail.email.content,
                date = System.currentTimeMillis(),
                senderName = fullEmail.from.name,
                signature = signature,
                template = template)
        return ComposerInputData(to = to, cc = cc, bcc = emptyList(),
                body = body, subject = subject, passwordForNonCriptextUsers = null, attachments = null, fileKey = null)
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
                    it.size)
        })

        return ComposerInputData(to = emptyList(), cc = emptyList(), bcc = emptyList(),
                body = body, subject = subject, passwordForNonCriptextUsers = null,
                attachments = if (attachments.isEmpty()) null else attachments, fileKey = fullEmail.fileKey)

    }

    private fun createSupportInputData(): ComposerInputData {
        val supportTemplate = (composerType as ComposerType.Support).template
        val supportContact= db.contactDao.getContact(supportTemplate.contact)

        return if(supportContact != null){
            ComposerInputData(to = listOf(supportContact), cc = emptyList(), bcc = emptyList(),
                    body = supportTemplate.body, subject = supportTemplate.subject,
                    passwordForNonCriptextUsers = null, attachments = null, fileKey = null)
        }else{
            val newSupportContact = Contact(id = 0, email = "support@${Contact.mainDomain}",
                    name = "Criptext Support")
            db.contactDao.insertAll(listOf(newSupportContact))
            ComposerInputData(to = listOf(newSupportContact), cc = emptyList(), bcc = emptyList(),
                    body = supportTemplate.body, subject = supportTemplate.subject,
                    passwordForNonCriptextUsers = null, attachments = null, fileKey = null)
        }
    }

    private fun createMailToInputData(to: String): ComposerInputData {
        val contact = db.contactDao.getContact(to)

        return if(contact != null){
            ComposerInputData(to = listOf(contact), cc = emptyList(), bcc = emptyList(),
                    body = "", subject = "", passwordForNonCriptextUsers = null, fileKey = null,
                    attachments = null)
        }else{
            val newContact = Contact(id = 0, email = to,
                    name = EmailAddressUtils.extractName(to))
            db.contactDao.insertAll(listOf(newContact))
            ComposerInputData(to = listOf(newContact), cc = emptyList(), bcc = emptyList(),
                    body = "", subject = "", passwordForNonCriptextUsers = null, attachments = null,
                    fileKey = null)
        }
    }

    private fun createComposerInputData(loadedEmail: FullEmail): ComposerInputData =
        when (composerType) {
                is ComposerType.Forward -> convertForwardToInputData(loadedEmail)
                is ComposerType.Reply -> convertReplyToInputData(loadedEmail, replyToAll = false)
                is ComposerType.ReplyAll -> convertReplyToInputData(loadedEmail, replyToAll = true)
                else -> convertDraftToInputData(loadedEmail)
            }

    override fun work(reporter: ProgressReporter<ComposerResult.LoadInitialData>): ComposerResult.LoadInitialData? {
        val loadedEmail = db.loadFullEmail(emailId)
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
