package com.email.scenes.composer.data

import com.email.bgworker.BackgroundWorker
import com.email.db.ComposerLocalDB
import com.email.db.ContactTypes
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContact
import com.email.db.models.EmailLabel
import com.email.utils.HTMLUtils
import java.util.*

/**
 * Created by danieltigse on 4/17/18.
 */
class SaveEmailWorker(
        private val fromRecipientId: String,
        private val composerInputData: ComposerInputData,
        private val db: ComposerLocalDB,
        private val onlySave: Boolean,
        override val publishFn: (ComposerResult.SaveEmail) -> Unit)
    : BackgroundWorker<ComposerResult.SaveEmail> {

    override val canBeParallelized = true

    override fun catchException(ex: Exception): ComposerResult.SaveEmail {
        return ComposerResult.SaveEmail.Failure()
    }

    override fun work(): ComposerResult.SaveEmail? {
        return ComposerResult.SaveEmail.Success(saveEmail(fromRecipientId,
                composerInputData), onlySave)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO delete this duplicated code
    private fun saveEmail(fromRecipientId: String, composerInputData: ComposerInputData): Int{

        val bodyContent = composerInputData.body
        val bodyWithoutHTML = HTMLUtils.html2text(bodyContent)
        val preview = if (bodyWithoutHTML.length > 20 )
            bodyWithoutHTML.substring(0,20)
        else bodyWithoutHTML

        val email = Email(
                id = 0,
                unread = true,
                date = Date(),
                threadid = "",
                subject = composerInputData.subject,
                isTrash = false,
                secure = true,
                preview = preview,
                key = "",
                isDraft = false,
                delivered = DeliveryTypes.SENT,
                content = bodyContent
        )
        val insertedId = db.emailDao.insert(email).toInt()

        createContacts(null, fromRecipientId, insertedId,
                ContactTypes.FROM)
        createContacts(null, composerInputData.to.joinToString(","),
                insertedId, ContactTypes.TO)
        createContacts(null, composerInputData.bcc.joinToString(","),
                insertedId, ContactTypes.BCC)
        createContacts(null, composerInputData.cc.joinToString(","),
                insertedId, ContactTypes.CC)

        val labelInbox = db.labelDao.get(MailFolders.DRAFT)
        db.emailLabelDao.insert(EmailLabel(
                labelId = labelInbox.id!!,
                emailId = insertedId))
        return insertedId

    }

    private fun createContacts(contactName: String?, contactEmail: String, insertedEmailId: Int, type: ContactTypes) {
        if(type == ContactTypes.FROM) {
            insertContact(
                    contactName = contactName,
                    contactEmail = contactEmail,
                    emailId = insertedEmailId,
                    type = type)
            return
        }

        val contactsList = contactEmail.split(",")
        contactsList.forEach { email ->
            insertContact(
                    contactName = contactName,
                    contactEmail = email,
                    emailId = insertedEmailId,
                    type = type)
        }
    }

    private fun insertContact(contactName: String?, contactEmail: String, emailId: Int, type: ContactTypes) {
        if(contactEmail.isNotEmpty()) {
            val contact = Contact(email = contactEmail, name = contactName ?: contactEmail)
            val emailContact = EmailContact(
                    contactId = contactEmail,
                    emailId = emailId,
                    type = type)
            db.contactDao.insert(contact)
            db.emailContactDao.insert(emailContact)
        }
    }

}

