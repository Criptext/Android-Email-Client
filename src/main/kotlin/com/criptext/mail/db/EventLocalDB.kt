package com.criptext.mail.db

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.api.models.TrackingUpdate
import com.criptext.mail.db.models.*
import com.criptext.mail.db.models.signal.CRPreKey
import com.criptext.mail.db.models.signal.CRSignedPreKey
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.scenes.mailbox.data.ExistingEmailUpdateSetup
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.*
import java.io.File
import java.util.*

class EventLocalDB(private val db: AppDatabase, private val filesDir: File, private val cacheDir: File){

    private val account by lazy { db.accountDao().getLoggedInAccount()!! }

    fun getExistingAccount(): Account? {
        return db.accountDao().getLoggedInAccount()
    }

    fun getAccountByRecipientId(recipientId: String?): Account? {
        if(recipientId == null) return null
        return db.accountDao().getAccountByRecipientId(recipientId)
    }

    fun setActiveAccount(id: Long) {
        db.accountDao().updateActiveInAccount()
        db.accountDao().updateActiveInAccount(id)
    }

    fun getLoggedAccounts(): List<Account> {
        return db.accountDao().getLoggedInAccounts()
    }

    fun getCacheDir(): File {
        return cacheDir
    }

    fun getAllPreKeys(): List<CRPreKey> {
        return db.rawPreKeyDao().getAll(account.id)
    }

    fun insertPreKeys(preKeys : List<CRPreKey>){
        db.rawPreKeyDao().insertAll(preKeys)
    }

    fun logoutNukeDB() {
        EmailUtils.deleteEmailsInFileSystem(filesDir, db.accountDao().getLoggedInAccount()!!.recipientId)
        db.clearAllTables()
    }

    fun logout(){
        db.accountDao().logoutAccount(account.id)
        db.rawIdentityKeyDao().deleteAll(account.id)
        db.rawPreKeyDao().deleteAll(account.id)
        db.rawSessionDao().deleteAll(account.id)
        db.rawSignedPreKeyDao().deleteAll(account.id)
    }

    fun getFromContactByEmailId(id: Long): List<Contact> {
        return db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
    }

    fun getEmailByMetadataKey(metadataKey: Long, accountId: Long): Email?{
        return db.emailDao().findEmailByMetadataKey(metadataKey, accountId)
    }

    fun getFullEmailById(emailId: Long, accountId: Long): FullEmail? {
        val email = db.emailDao().getEmailById(emailId, accountId) ?: return null
        val id = email.id
        val labels = db.emailLabelDao().getLabelsFromEmail(id)
        val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
        val files = db.fileDao().getAttachmentsFromEmail(id)
        val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)

        val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                email.metadataKey, email.content,
                db.accountDao().getLoggedInAccount()!!.recipientId)

        return FullEmail(
                email = email.copy(content = emailContent.first),
                bcc = contactsBCC,
                cc = contactsCC,
                from = contactsFROM[0],
                files = files,
                labels = labels,
                to = contactsTO, fileKey = fileKey?.key, headers = emailContent.second)


    }

    fun removeDevice(storage: KeyValueStorage){
        db.clearAllTables()
        storage.clearAll()
    }

    fun updateFeedItems(trackingUpdates: List<TrackingUpdate>) {
        val feeds = mutableListOf<FeedItem>()
        trackingUpdates.forEach {
            val existingEmail = db.emailDao().findEmailByMetadataKey(it.metadataKey, account.id)
            if(existingEmail != null && it.type == DeliveryTypes.READ){
                feeds.add(FeedItem(
                        id = 0,
                        date = Date(),
                        feedType = FeedType.OPEN_EMAIL,
                        location = "",
                        seen = false,
                        emailId = existingEmail.id,
                        contactId = db.contactDao().getContact("${it.from}@${Contact.mainDomain}", account.id)!!.id,
                        fileId = null
                ))
            }
        }

        db.feedDao().insertFeedItems(feeds)
    }

    fun updateDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes) {
        if (metadataKeys.isNotEmpty()) {
            db.emailDao().changeDeliveryTypeByMetadataKey(metadataKeys, deliveryType,
                    listOf(DeliveryTypes.getTrueOrdinal(DeliveryTypes.UNSEND),
                    DeliveryTypes.getTrueOrdinal(DeliveryTypes.NONE)), account.id)
        }
    }

    fun updateExistingEmail(emailMetadata: EmailMetadata, activeAccount: ActiveAccount) {
        ExistingEmailUpdateSetup.updateExistingEmailTransaction(metadata = emailMetadata, dao = db.emailInsertionDao(),
                activeAccount = activeAccount)
    }

    fun updateCreateLabel(text: String, color: String, uuid: String, accountId: Long) {
        db.labelDao().insert(Label(
                id = 0,
                text = text,
                color = ColorUtils.colorStringByName(color),
                visible = true,
                type = LabelTypes.CUSTOM,
                uuid = uuid,
                accountId = accountId
        ))
    }

    fun updateDeleteThreadPermanently(threadIds: List<String>) {
        if(threadIds.isNotEmpty()){
            db.emailDao().getEmailsFromThreadIds(threadIds, account.id).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = db.accountDao().getLoggedInAccount()!!.recipientId)
            }
            db.emailDao().deleteThreads(threadIds, listOf(Label.defaultItems.trash.id, Label.defaultItems.spam.id), account.id)
        }
    }

    fun updateDeleteEmailPermanently(metadataKeys: List<Long>) {
        if(metadataKeys.isNotEmpty()){
            val emails = db.emailDao().getAllEmailsByMetadataKey(metadataKeys, account.id)
            if(emails.isEmpty()) return
            emails.forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = db.accountDao().getLoggedInAccount()!!.recipientId)
            }
            db.emailDao().deleteAll(emails)
        }
    }

    fun updateThreadLabels(threadIds: List<String>, labelsAdded: List<String>, labelsRemoved: List<String>) {
        if(threadIds.isNotEmpty()){

            val systemLabels = db.labelDao().get(Label.defaultItems.toList()
                    .filter { it.text !in listOf(Label.LABEL_SPAM, Label.LABEL_TRASH, Label.LABEL_STARRED) }
                    .map { it.text }, account.id)
            val threads = db.emailDao().getEmailsFromThreadIds(threadIds, account.id)
            if(threads.isEmpty()) return
            val emailIds = threads.map { it.id }
            val removedLabels = db.labelDao().get(labelsRemoved, account.id)
            val removedNonSystemLabelIds = removedLabels.filter { !systemLabels.contains(it) }.map { it.id }
            val addedLabels = db.labelDao().get(labelsAdded, account.id)

            db.emailLabelDao().deleteRelationByLabelsAndEmailIds(removedNonSystemLabelIds, emailIds)

            if(Label.defaultItems.trash in removedLabels){
                db.emailLabelDao().deleteRelationByLabelsAndEmailIds(listOf(Label.defaultItems.trash.id), emailIds)
            }

            if(Label.defaultItems.trash in addedLabels){
                db.emailDao().updateEmailTrashDate(Date(), emailIds, account.id)
            }


            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabels.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            db.emailLabelDao().insertAll(labelEmails)
        }
    }

    fun updateEmailLabels(metadataKeys: List<Long>, labelsAdded: List<String>, labelsRemoved: List<String>) {
        if(metadataKeys.isNotEmpty()){

            val systemLabels = db.labelDao().get(Label.defaultItems.toList()
                    .filter { it.text !in listOf(Label.LABEL_SPAM, Label.LABEL_TRASH, Label.LABEL_STARRED) }
                    .map { it.text }, account.id)
            val emails = db.emailDao().getAllEmailsByMetadataKey(metadataKeys, account.id)
            if(emails.isEmpty()) return
            val emailIds = emails.map { it.id }
            val removedLabels = db.labelDao().get(labelsRemoved, account.id)
            val removedNonSystemLabelIds = removedLabels.filter { !systemLabels.contains(it) }.map { it.id }
            val addedLabels = db.labelDao().get(labelsAdded, account.id)

            db.emailLabelDao().deleteRelationByLabelsAndEmailIds(removedNonSystemLabelIds, emailIds)

            if(Label.defaultItems.trash in removedLabels){
                db.emailLabelDao().deleteRelationByLabelsAndEmailIds(listOf(Label.defaultItems.trash.id), emailIds)
            }

            if(Label.defaultItems.trash in addedLabels){
                db.emailDao().updateEmailTrashDate(Date(), emailIds, account.id)
            }

            val selectedLabels = SelectedLabels()
            val labelsWrapper = addedLabels.map { LabelWrapper(it) }
            selectedLabels.addMultipleSelected(labelsWrapper)
            val labelEmails = emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
            db.emailLabelDao().insertAll(labelEmails)
        }
    }

    fun updateUserName(recipientId: String, name: String) {
        db.contactDao().updateContactName("$recipientId@${Contact.mainDomain}", name, account.id)
        db.accountDao().updateProfileName(name, recipientId)
    }

    fun updateUnsendStatusByMetadataKey(metadataKey: Long, unsentDate: Date) {
        db.emailDao().changeDeliveryTypeByMetadataKey(metadataKey, DeliveryTypes.UNSEND, account.id)
        db.emailDao().unsendEmailByMetadataKey(metadataKey, "", "",
                unsentDate, account.id)
        db.fileDao().changeFileStatusByEmailid(db.emailDao().getEmailByMetadataKey(metadataKey, account.id).id, 0)
        EmailUtils.deleteEmailInFileSystem(
                filesDir = filesDir,
                metadataKey = metadataKey,
                recipientId = db.accountDao().getLoggedInAccount()!!.recipientId
        )
    }

    fun updateUnreadStatusByThreadId(emailThreads: List<String>, updateUnreadStatus: Boolean) {
        db.emailDao().toggleReadByThreadId(emailThreads, updateUnreadStatus, account.id)
    }

    fun updateUnreadStatusByMetadataKeys(metadataKeys: List<Long>, updateUnreadStatus: Boolean) {
        db.emailDao().toggleReadByMetadataKey(metadataKeys, updateUnreadStatus, account.id)
    }

    fun insertIncomingEmail(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                     metadata: EmailMetadata, activeAccount: ActiveAccount) {
        EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                dao = db.emailInsertionDao(), apiClient = apiClient, metadata = metadata,
                activeAccount = activeAccount, filesDir = filesDir)
    }

    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String): EmailThread {

        val id = email.id
        val labels = db.emailLabelDao().getLabelsFromEmail(id)
        val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
        val files = db.fileDao().getAttachmentsFromEmail(id)
        val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)
        email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

        val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels, account.id)
        var totalFiles = 0
        val participants = emails.flatMap {
            val contacts = mutableListOf<Contact>()
            if(selectedLabel == Label.defaultItems.sent.text){
                val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                if(EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)){
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                }
            }
            else{
                contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM))
            }
            contacts.map { contact ->
                if(contact.email == userEmail){
                    //It's difficult to reach String resources, so I will leave the 'me' string for now
                    contact.name = "me"
                }
            }
            totalFiles += db.fileDao().getAttachmentsFromEmail(it.id).size
            contacts
        }

        val fromContact = if(EmailAddressUtils.checkIfOnlyHasEmail(email.fromAddress)){
            contactsFROM[0]
        }else Contact(
                id = 0,
                email = EmailAddressUtils.extractEmailAddress(email.fromAddress),
                name = EmailAddressUtils.extractName(email.fromAddress),
                isTrusted = contactsFROM[0].isTrusted,
                score = contactsFROM[0].score
        )
        val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                email.metadataKey, email.content,
                db.accountDao().getLoggedInAccount()!!.recipientId)

        return EmailThread(
                participants = participants.distinctBy { it.id },
                currentLabel = selectedLabel,
                latestEmail = FullEmail(
                        email = email.copy(content = emailContent.first),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = fromContact,
                        files = files,
                        labels = labels,
                        to = contactsTO,
                        fileKey = fileKey?.key,
                        headers = emailContent.second),
                totalEmails = emails.size,
                hasFiles = totalFiles > 0,
                allFilesAreInline = files.filter { it.cid != null }.size == totalFiles
        )
    }

    fun getThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                            startDate: Date?, limit: Int,
                                            rejectedLabels: List<Label>): List<EmailThread> {
        val account = db.accountDao().getLoggedInAccount()!!
        val labels = db.labelDao().getAll(account.id)
        val selectedLabel = Label.getLabelIdWildcard(labelName, labels)
        val conditionalLabels = listOf(
                Label.getLabelIdWildcard(Label.LABEL_TRASH, labels),
                Label.getLabelIdWildcard(Label.LABEL_SPAM, labels)
        )
        val rejectedIdLabels = rejectedLabels.filter {label ->
            label.text != labelName
        }.map {
            it.id
        }
        var emails = if(startDate != null)
            db.emailDao().getEmailThreadsFromMailboxLabel(
                    isTrashOrSpam = (selectedLabel in conditionalLabels),
                    startDate = startDate,
                    rejectedLabels = rejectedIdLabels,
                    selectedLabel = selectedLabel,
                    limit = limit,
                    accountId = account.id)

        else
            db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                    isTrashOrSpam = (selectedLabel in conditionalLabels),
                    rejectedLabels = rejectedIdLabels,
                    selectedLabel = selectedLabel,
                    limit = limit,
                    accountId = account.id)

        emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                filesDir, it.metadataKey, it.content,
                db.accountDao().getLoggedInAccount()!!.recipientId).first) }

        return emails.map { email ->
            getEmailThreadFromEmail(email, labelName,
                    Label.defaultItems.rejectedLabelsByMailbox(
                            db.labelDao().get(labelName, account.id)
                    ).map { it.id }, userEmail)
        } as ArrayList<EmailThread>
    }

    fun getThreadIdsFromTrashExpiredEmails(): List<Long>{
        val labelId = Label.defaultItems.trash.id
        return db.emailDao().getTrashExpiredThreadIds(labelId, account.id)
    }

}