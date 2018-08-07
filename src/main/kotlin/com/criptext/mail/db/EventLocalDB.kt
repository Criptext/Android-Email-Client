package com.criptext.mail.db

import com.criptext.mail.SecureEmail
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.api.models.TrackingUpdate
import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.ExistingEmailUpdateSetup
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.ColorUtils
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.EmailThreadValidator
import java.util.*

class EventLocalDB(private val emailDao: EmailDao, private val labelDao: LabelDao,
                   private val contactDao: ContactDao, private val feedDao: FeedItemDao,
                   private val emailInsertionDao: EmailInsertionDao,
                   private val emailLabelDao: EmailLabelDao, private val accountDao: AccountDao,
                   private val emailContactDao: EmailContactJoinDao, private val fileDao: FileDao,
                   private val fileKeyDao: FileKeyDao){


    constructor (db: AppDatabase): this(emailDao = db.emailDao(), accountDao = db.accountDao(),
            contactDao = db.contactDao(), emailLabelDao = db.emailLabelDao(), labelDao = db.labelDao(),
            fileDao = db.fileDao(), emailContactDao = db.emailContactDao(),
            emailInsertionDao = db.emailInsertionDao(), feedDao = db.feedDao(), fileKeyDao = db.fileKeyDao())

    fun updateFeedItems(trackingUpdates: List<TrackingUpdate>) {
        val feeds = mutableListOf<FeedItem>()
        trackingUpdates.forEach {
            val existingEmail = emailDao.findEmailByMetadataKey(it.metadataKey)
            if(existingEmail != null && it.type == DeliveryTypes.READ){
                feeds.add(FeedItem(
                        id = 0,
                        date = Date(),
                        feedType = FeedType.OPEN_EMAIL,
                        location = "",
                        seen = false,
                        emailId = existingEmail.id,
                        contactId = contactDao.getContact("${it.from}@${Contact.mainDomain}")!!.id,
                        fileId = null
                ))
            }
        }

        feedDao.insertFeedItems(feeds)
    }

    fun updateDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) {
        if (metadataKeys.isNotEmpty()) {
            emailDao.changeDeliveryTypeByMetadataKey(metadataKeys, deliveryType, DeliveryTypes.UNSEND)
        }
    }

    fun updateExistingEmail(emailMetadata: EmailMetadata, activeAccount: ActiveAccount) {
        ExistingEmailUpdateSetup.updateExistingEmailTransaction(metadata = emailMetadata, dao = emailInsertionDao,
                activeAccount = activeAccount)
    }

    fun updateCreateLabel(text: String, color: String) {
        labelDao.insert(Label(
                id = 0,
                text = text,
                color = ColorUtils.colorStringByName(color),
                visible = true,
                type = LabelTypes.CUSTOM
        ))
    }

    fun updateDeleteThreadPermanently(threadIds: List<String>) {
        if(!threadIds.isEmpty()){
            emailDao.deleteThreads(threadIds)
        }
    }

    fun updateDeleteEmailPermanently(metadataKeys: List<Long>) {
        if(!metadataKeys.isEmpty()){
            emailDao.deleteAll(emailDao.getAllEmailsByMetadataKey(metadataKeys))
        }
    }

    fun updateThreadLabels(threadIds: List<String>, labelsAdded: List<String>, labelsRemoved: List<String>) {
        if(!threadIds.isEmpty()){

            val emailIds = emailDao.getEmailsFromThreadIds(threadIds).map { it.id }
            val removedLabelIds = labelDao.get(labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)


            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabelIds.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            emailLabelDao.insertAll(labelEmails)
        }
    }

    fun updateEmailLabels(metadataKeys: List<Long>, labelsAdded: List<String>, labelsRemoved: List<String>) {
        if(!metadataKeys.isEmpty()){

            val emailIds = emailDao.getAllEmailsByMetadataKey(metadataKeys).map { it.id }
            val removedLabelIds = labelDao.get(labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)


            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabelIds.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            emailLabelDao.insertAll(labelEmails)
        }
    }

    fun updateUserName(recipientId: String, name: String) {
        contactDao.updateContactName("$recipientId@${Contact.mainDomain}", name)
        accountDao.updateProfileName(name, recipientId)
    }

    fun updateUnsendStatusByMetadataKey(metadataKey: Long, unsentDate: String) {
        emailDao.changeDeliveryTypeByMetadataKey(metadataKey, DeliveryTypes.UNSEND)
        emailDao.unsendEmailByMetadataKey(metadataKey, "", "",
                DateUtils.getDateFromString(unsentDate, null))
        fileDao.changeFileStatusByEmailid(emailDao.getEmailByMetadataKey(metadataKey).id, 0)
    }

    fun updateUnreadStatusByThreadId(emailThreads: List<String>, updateUnreadStatus: Boolean) {
        emailDao.toggleReadByThreadId(emailThreads, updateUnreadStatus)
    }

    fun insertIncomingEmail(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                     metadata: EmailMetadata, activeAccount: ActiveAccount) {
        EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                dao = emailInsertionDao, apiClient = apiClient, metadata = metadata,
                activeAccount = activeAccount)
    }

    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String): EmailThread {

        val id = email.id
        val labels = emailLabelDao.getLabelsFromEmail(id)
        val contactsCC = emailContactDao.getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = emailContactDao.getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = emailContactDao.getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = emailContactDao.getContactsFromEmail(id, ContactTypes.TO)
        val files = fileDao.getAttachmentsFromEmail(id)
        val fileKey = fileKeyDao.getAttachmentKeyFromEmail(id)
        email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

        val emails = emailDao.getEmailsFromThreadId(email.threadId, rejectedLabels)
        var totalFiles = 0
        val participants = emails.flatMap {
            val contacts = mutableListOf<Contact>()
            if(selectedLabel == Label.defaultItems.sent.text){
                val emailLabels = emailLabelDao.getLabelsFromEmail(it.id)
                if(EmailThreadValidator.isLabelInList(emailLabels, SecureEmail.LABEL_SENT)){
                    contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.TO))
                    contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.CC))
                }
            }
            else{
                contacts.addAll(emailContactDao.getContactsFromEmail(it.id, ContactTypes.FROM))
            }
            contacts.map { contact ->
                if(contact.email == userEmail){
                    //It's difficult to reach String resources, so I will leave the 'me' string for now
                    contact.name = "me"
                }
            }
            totalFiles += fileDao.getAttachmentsFromEmail(it.id).size
            contacts
        }

        return EmailThread(
                participants = participants.distinctBy { it.id },
                currentLabel = selectedLabel,
                latestEmail = FullEmail(
                        email = email,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO,
                        fileKey = fileKey.key),
                totalEmails = emails.size,
                hasFiles = totalFiles > 0
        )
    }

    fun getThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                            startDate: Date?, limit: Int,
                                            rejectedLabels: List<Label>): List<EmailThread> {

        val labels = labelDao.getAll()
        val selectedLabel = if(labelName == SecureEmail.LABEL_ALL_MAIL) "%" else
            "%${labels.findLast {
                label ->label.text == labelName
            }?.id}%"
        val rejectedIdLabels = rejectedLabels.filter {label ->
            label.text != labelName
        }.map {
            it.id
        }
        val emails = if(startDate != null)
            emailDao.getEmailThreadsFromMailboxLabel(
                    startDate = startDate,
                    rejectedLabels = rejectedIdLabels,
                    selectedLabel = selectedLabel,
                    limit = limit )

        else
            emailDao.getInitialEmailThreadsFromMailboxLabel(
                    rejectedLabels = rejectedIdLabels,
                    selectedLabel = selectedLabel,
                    limit = limit )

        return emails.map { email ->
            getEmailThreadFromEmail(email, labelName,
                    Label.defaultItems.rejectedLabelsByMailbox(
                            labelDao.get(labelName)
                    ).map { it.id }, userEmail)
        } as ArrayList<EmailThread>
    }

}