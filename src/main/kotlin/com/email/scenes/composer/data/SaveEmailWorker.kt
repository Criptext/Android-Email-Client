package com.email.scenes.composer.data

import com.email.api.models.EmailMetadata
import com.email.bgworker.BackgroundWorker
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.scenes.mailbox.data.EmailInsertionSetup
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
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
        private val onlySave: Boolean,
        override val publishFn: (ComposerResult.SaveEmail) -> Unit)
    : BackgroundWorker<ComposerResult.SaveEmail> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.SaveEmail {
        return ComposerResult.SaveEmail.Failure()
    }

    override fun work(): ComposerResult.SaveEmail? {
        if(emailId != null){

        }
        saveEmail()
        return ComposerResult.SaveEmail.Success(0L, onlySave)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                fromContact = sender
                )
    }

    private fun createDraftMessageId(deviceId: Int): String =
            "${System.currentTimeMillis()}:$deviceId"

    private fun saveEmail() {
        val metadataColumns = createMetadataColumns()
        val defaultLabels = Label.DefaultItems()
        val labels = listOf(defaultLabels.draft)
        dao.runTransaction(Runnable {
            if (emailId != null) dao.deletePreviouslyCreatedDraft(emailId)
            EmailInsertionSetup.exec(dao = dao, metadataColumns = metadataColumns,
                    decryptedBody = composerInputData.body, labels = labels)
        })
    }
}

