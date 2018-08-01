package com.criptext.mail.scenes.composer.data

import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.dao.FileKeyDao
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.file.FileUtils
import java.util.*

/**
 * Created by danieltigse on 4/17/18.
 */
class SaveEmailWorker(
        private val threadId: String?,
        private val emailId: Long?,
        private val composerInputData: ComposerInputData,
        private val account: ActiveAccount,
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

    override fun work(reporter: ProgressReporter<ComposerResult.SaveEmail>)
            : ComposerResult.SaveEmail? {
        val (newEmailId, savedMailThreadId) = saveEmail()
        return ComposerResult.SaveEmail.Success(emailId = newEmailId, threadId = savedMailThreadId,
                onlySave = onlySave, composerInputData = composerInputData, attachments = attachments,
                fileKey = fileKey)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
    private val selectEmail: (Contact) -> String = { contact -> contact.email }

    private fun List<Contact>.toCSVEmails(): String =
            this.joinToString(separator = ",", transform = selectEmail)

    private fun createMetadataColumns(): EmailMetadata.DBColumns {
        val draftMessageId = createDraftMessageId(account.deviceId)
        val sender = Contact(id = 0, name = account.name,
                email = "${account.recipientId}@${Contact.mainDomain}")

        return EmailMetadata.DBColumns(messageId = draftMessageId,
                threadId = threadId ?: draftMessageId, subject = composerInputData.subject,
                to = composerInputData.to.toCSVEmails(),
                cc = composerInputData.cc.toCSVEmails(),
                bcc = composerInputData.bcc.toCSVEmails(),
                date = DateUtils.printDateWithServerFormat(Date()),
                unsentDate = DateUtils.printDateWithServerFormat(Date()),
                metadataKey = System.currentTimeMillis(), // ugly hack because we don't have draft table
                fromContact = sender,
                unread = false,
                status = if(onlySave) DeliveryTypes.NONE else DeliveryTypes.SENDING)
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
                    readOnly = false,
                    emailId = 0
            )
        }

    private fun saveEmail(): Pair<Long, String> {
        val metadataColumns = createMetadataColumns()
        val defaultLabels = Label.DefaultItems()
        val labels = if(onlySave) listOf(defaultLabels.draft) else listOf(defaultLabels.sent)
        val files = createFilesData()
        val newEmailId = dao.runTransaction({
            if (emailId != null) dao.deletePreviouslyCreatedDraft(emailId)

            EmailInsertionSetup.exec(dao = dao, metadataColumns = metadataColumns,
                    decryptedBody = composerInputData.body, labels = labels, files = files, fileKey = fileKey)

        })

        return Pair(newEmailId, metadataColumns.threadId)
    }
}

