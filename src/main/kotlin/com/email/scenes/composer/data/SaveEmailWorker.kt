package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.scenes.mailbox.data.EmailInsertionSetup
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
            // TODO delete draft?
        }
        saveEmail()
        return ComposerResult.SaveEmail.Success(0L, onlySave)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun createEmailFromComposerInputData(): FullEmail {
        val bodyContent = composerInputData.body
        val preview = HTMLUtils.createEmailPreview(bodyContent)
        val draftMessageId = createDraftMessageId(account.deviceId)
        val email = Email(
                id = 0,
                unread = false,
                date = Date(),
                threadId = threadId ?: draftMessageId,
                subject = composerInputData.subject,
                secure = true,
                preview = preview,
                messageId = draftMessageId,
                isDraft = false,
                delivered = DeliveryTypes.NONE,
                content = bodyContent
        )
        val defaultLabels = Label.DefaultItems()
        val sender = Contact(id = 0, name = account.name,
                email = "${account.recipientId}@${Contact.mainDomain}")

        return FullEmail(email = email, labels = listOf(defaultLabels.draft),
            to = composerInputData.to, cc = composerInputData.cc, bcc = composerInputData.bcc,
            files = emptyList(), from = sender)
    }


    private fun createDraftMessageId(deviceId: Int): String =
            "${System.currentTimeMillis()}:$deviceId"

    private fun saveEmail() {
        val fullEmail = createEmailFromComposerInputData()
        dao.runTransaction(Runnable {
            EmailInsertionSetup.exec(dao, fullEmail)
        })
    }
}

