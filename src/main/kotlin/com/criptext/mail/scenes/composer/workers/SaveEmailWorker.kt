package com.criptext.mail.scenes.composer.workers

import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.composer.Validator
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailUtils
import com.criptext.mail.utils.HTMLUtils
import com.criptext.mail.utils.file.FileUtils
import com.github.kittinunf.result.Result
import java.io.File
import java.util.*

/**
 * Created by danieltigse on 4/17/18.
 */
class SaveEmailWorker(
        private val goToRecoveryEmail: Boolean,
        private val filesDir: File,
        private val originalId: Long?,
        private val threadId: String?,
        private val emailId: Long?,
        private val currentLabel: Label,
        private val db: ComposerLocalDB,
        private val composerInputData: ComposerInputData,
        private val senderAddress: String,
        private val activeAccount: ActiveAccount,
        private val dao: EmailInsertionDao,
        private val fileKey: String?,
        private val onlySave: Boolean,
        private val attachments: List<ComposerAttachment>,
        override val publishFn: (ComposerResult.SaveEmail) -> Unit)
    : BackgroundWorker<ComposerResult.SaveEmail> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.SaveEmail {
        return ComposerResult.SaveEmail.Failure()
    }

    private var account: ActiveAccount = activeAccount
    private var aliasEmail: String? = null

    override fun work(reporter: ProgressReporter<ComposerResult.SaveEmail>)
            : ComposerResult.SaveEmail? {
        val setupOperation = Result.of { setup() }

        when(setupOperation){
            is Result.Success -> {
                if(isRecipientLimitReached()) return ComposerResult.SaveEmail.TooManyRecipients()

                val (newEmailId, savedMailThreadId) = saveEmail()
                val attachmentsSaved = dao.findFilesByEmailId(newEmailId).map {
                    ComposerAttachment(
                            id = it.id, uuid = UUID.randomUUID().toString(), fileKey = it.fileKey, size = it.size,
                            filepath = attachments.find { file -> it.token == file.filetoken }!!.filepath,
                            filetoken = it.token, type = attachments.find { file -> it.token == file.filetoken }!!.type,
                            uploadProgress = attachments.find { file -> it.token == file.filetoken }!!.uploadProgress,
                            cid = it.cid
                    )
                }
                return ComposerResult.SaveEmail.Success(emailId = newEmailId, threadId = savedMailThreadId,
                        onlySave = onlySave, composerInputData = composerInputData, attachments = attachmentsSaved,
                        fileKey = fileKey, preview = if(threadId != null) EmailPreview.fromEmailThread(getEmailPreview(newEmailId)) else null,
                        senderAddress = aliasEmail, account = account, goToRecoveryEmail = goToRecoveryEmail)
            }
            is Result.Failure -> {
                catchException(setupOperation.error)
            }
        }
        return ComposerResult.SaveEmail.Failure()
    }

    private fun setup(){
        val domain = EmailAddressUtils.extractEmailAddressDomain(senderAddress)
        if(domain.isNotEmpty()){
            val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(senderAddress, domain)
            var dbAccount = db.accountDao.getAccount(recipientId, domain)
            if(dbAccount == null){
                val alias = (if(domain == Contact.mainDomain) {
                    db.aliasDao.getCriptextAliasByName(recipientId)
                } else {
                    db.aliasDao.getAliasByName(recipientId, domain)
                }) ?: throw Exception()
                aliasEmail = alias.name.plus("@${alias.domain ?: Contact.mainDomain}")
                dbAccount = db.accountDao.getAccountById(alias.accountId) ?: throw Exception()
            }
            account = ActiveAccount.loadFromDB(dbAccount)!!
        } else {
            throw Exception()
        }
    }

    private fun getEmailPreview(emailId: Long): EmailThread {
        val email = db.loadFullEmail(emailId, account)!!
        val label = db.getLabelById(currentLabel.id, account.id) ?: Label.defaultItems.inbox
        return db.getEmailThreadFromEmail(email.email, label.text, Label.defaultItems.rejectedLabelsByFolder(label.text).map { it.id },
                account.userEmail, account)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

    private fun isRecipientLimitReached(): Boolean{
        val sumOfContacts = composerInputData.to.size + composerInputData.cc.size +
                composerInputData.bcc.size
        return sumOfContacts > EmailUtils.RECIPIENT_LIMIT
    }

    private fun isSecure(): Boolean{
        if(!Validator.criptextOnlyContacts(composerInputData)){
            return false
        }
        return true
    }

    private fun createMetadataColumns(): EmailMetadata.DBColumns {
        val draftMessageId = createDraftMessageId(account.deviceId)
        val senderEmail = aliasEmail ?: (account.userEmail)
        val sender = Contact(id = 0, name = account.name,
                email = senderEmail,
                isTrusted = true, score = 0, spamScore = 0)

        val tempThreadId = System.currentTimeMillis()

        return EmailMetadata.DBColumns(messageId = draftMessageId,
                threadId = threadId ?: tempThreadId.toString(),
                subject = composerInputData.subject,
                to = composerInputData.to.map { it.email },
                cc = composerInputData.cc.map { it.email },
                bcc = composerInputData.bcc.map { it.email },
                date = DateAndTimeUtils.printDateWithServerFormat(Date()),
                unsentDate = DateAndTimeUtils.printDateWithServerFormat(Date()),
                metadataKey = tempThreadId, // ugly hack because we don't have draft table
                fromContact = sender,
                unread = meAsRecipient(),
                status = if(onlySave) DeliveryTypes.NONE else DeliveryTypes.SENDING,
                secure = isSecure(),
                replyTo = null,
                trashDate = DateAndTimeUtils.printDateWithServerFormat(Date()),
                boundary = null,
                isNewsletter = null)
    }

    private fun meAsRecipient(): Boolean {
        return composerInputData.bcc.map { it.email }.contains(account.userEmail)
                || composerInputData.cc.map { it.email }.contains(account.userEmail)
                || composerInputData.to.map { it.email }.contains(account.userEmail)
                || (aliasEmail != null && composerInputData.to.map { it.email }.contains(aliasEmail!!))
                || (aliasEmail != null && composerInputData.cc.map { it.email }.contains(aliasEmail!!))
                || (aliasEmail != null && composerInputData.bcc.map { it.email }.contains(aliasEmail!!))
    }


    private fun createDraftMessageId(deviceId: Int): String =
            "${System.currentTimeMillis()}:$deviceId"

    private fun createFilesData(): List<CRFile> =
        attachments.map {
            CRFile(
                    id = 0,
                    token = it.filetoken,
                    name = FileUtils.getName(it.filepath),
                    size = it.size,
                    status = 1,
                    date = Date(),
                    emailId = 0,
                    shouldDuplicate = shouldDuplicateFile(it.filetoken),
                    fileKey = it.fileKey,
                    cid = it.cid
            )
        }

    private fun shouldDuplicateFile(fileToken: String): Boolean{
        if(originalId == null) return false
        val email = dao.findEmailById(originalId, account.id) ?: return false
        val oldFiles = dao.findFilesByEmailId(email.id)
        if(fileToken in oldFiles.map { it.token })
            return true
        return false
    }

    private fun saveEmail(): Pair<Long, String> {
        val metadataColumns = createMetadataColumns()
        val defaultLabels = Label.DefaultItems()
        val labels = if(onlySave) listOf(defaultLabels.draft) else listOf(defaultLabels.sent)
        val files = createFilesData()
        val newEmailId = dao.runTransaction {
            if (emailId != null) dao.deletePreviouslyCreatedDraft(emailId, account.id)

            EmailInsertionSetup.exec(dao = dao, metadataColumns = metadataColumns, accountId = account.id,
                    preview = HTMLUtils.createEmailPreview(composerInputData.body), labels = labels, files = files, fileKey = fileKey)

        }

        EmailUtils.saveEmailInFileSystem(
                filesDir = filesDir,
                recipientId = account.recipientId,
                metadataKey = metadataColumns.metadataKey,
                content = composerInputData.body,
                headers = null,
                domain = account.domain)

        return Pair(newEmailId, metadataColumns.threadId)
    }
}

