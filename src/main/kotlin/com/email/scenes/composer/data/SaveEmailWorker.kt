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
        return ComposerResult.SaveEmail.Success(saveEmail(composerInputData), onlySave)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO delete this duplicated code
    private fun saveEmail(composerInputData: ComposerInputData): Long{

        val bodyContent = composerInputData.body
        val bodyWithoutHTML = HTMLUtils.html2text(bodyContent)
        val preview = if (bodyWithoutHTML.length > 100 )
            bodyWithoutHTML.substring(0,100)
        else bodyWithoutHTML

        val email = Email(
                id = 0,
                unread = false,
                date = Date(),
                threadid = "",
                subject = composerInputData.subject,
                isTrash = false,
                secure = true,
                preview = preview,
                key = "",
                isDraft = false,
                delivered = DeliveryTypes.NONE,
                content = bodyContent
        )
        val insertedId = db.emailDao.insert(email)

        val account = db.accountDao.getLoggedInAccount()!!
        insertContact(account.name, "${account.recipientId}@${Contact.mainDomain}",
                insertedId, ContactTypes.FROM)

        createContacts(composerInputData.to, insertedId, ContactTypes.TO)
        createContacts(composerInputData.bcc, insertedId, ContactTypes.BCC)
        createContacts(composerInputData.cc, insertedId, ContactTypes.CC)

        val labelInbox = db.labelDao.get(MailFolders.DRAFT)
        db.emailLabelDao.insert(EmailLabel(
                labelId = labelInbox.id,
                emailId = insertedId))
        return insertedId

    }

    private fun createContacts(contacts: List<Contact>, insertedEmailId: Long, type: ContactTypes) {

        contacts.map { contact ->
            insertContact(
                    contactName = contact.name,
                    contactEmail = contact.email,
                    emailId = insertedEmailId,
                    type = type)
        }

    }

    private fun insertContact(contactName: String?, contactEmail: String, emailId: Long,
                              type: ContactTypes) {
        if(contactEmail.isNotEmpty()) {
            val contact = Contact(id = 0, email = contactEmail, name = contactName ?: contactEmail)
            var contactId = db.contactDao.insertIgnoringConflicts(contact)
            if(contactId < 0){
                contactId = db.contactDao.getContact(contactEmail)!!.id
            }
            val emailContact = EmailContact(
                    contactId = contactId,
                    emailId = emailId,
                    type = type)
            db.emailContactDao.insert(emailContact)
        }
    }

}

