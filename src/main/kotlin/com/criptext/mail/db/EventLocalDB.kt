package com.criptext.mail.db

import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.api.models.TrackingUpdate
import com.criptext.mail.db.models.*
import com.criptext.mail.db.models.signal.CRPreKey
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

    fun getAccount(recipientId: String?, domain: String?): Account? {
        if(recipientId == null || domain == null) return null
        return db.accountDao().getAccount(recipientId, domain)
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

    fun getAllPreKeys(accountId: Long): List<CRPreKey> {
        return db.rawPreKeyDao().getAll(accountId)
    }

    fun insertPreKeys(preKeys : List<CRPreKey>){
        db.rawPreKeyDao().insertAll(preKeys)
    }

    fun logoutNukeDB(activeAccount: ActiveAccount) {
        EmailUtils.deleteEmailsInFileSystem(filesDir, activeAccount.recipientId, activeAccount.domain)
        db.accountDao().deleteAccountById(activeAccount.id)
    }

    fun logout(accountId: Long){
        db.accountDao().logoutAccount(accountId)
        db.rawIdentityKeyDao().deleteAll(accountId)
        db.rawPreKeyDao().deleteAll(accountId)
        db.rawSessionDao().deleteAll(accountId)
        db.rawSignedPreKeyDao().deleteAll(accountId)
    }

    fun getFromContactByEmailId(id: Long): List<Contact> {
        return db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
    }

    fun getEmailByMetadataKey(metadataKey: Long, accountId: Long): Email?{
        return db.emailDao().findEmailByMetadataKey(metadataKey, accountId)
    }

    fun getFullEmailById(emailId: Long, activeAccount: ActiveAccount): FullEmail? {
        val email = db.emailDao().getEmailById(emailId, activeAccount.id) ?: return null
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
                activeAccount.recipientId, activeAccount.domain)

        return FullEmail(
                email = email.copy(content = emailContent.first),
                bcc = contactsBCC,
                cc = contactsCC,
                from = contactsFROM[0],
                files = files,
                labels = labels,
                to = contactsTO, fileKey = fileKey?.key, headers = emailContent.second)


    }

    fun updateFeedItems(trackingUpdates: List<TrackingUpdate>, accountId: Long) {
        val feeds = mutableListOf<FeedItem>()
        trackingUpdates.forEach {
            val existingEmail = db.emailDao().findEmailByMetadataKey(it.metadataKey, accountId)
            if(existingEmail != null && it.type == DeliveryTypes.READ){
                feeds.add(FeedItem(
                        id = 0,
                        date = Date(),
                        feedType = FeedType.OPEN_EMAIL,
                        location = "",
                        seen = false,
                        emailId = existingEmail.id,
                        contactId = db.contactDao().getContact(it.from, accountId)!!.id,
                        fileId = null
                ))
            }
        }

        db.feedDao().insertFeedItems(feeds)
    }

    fun updateDeliveryTypeByMetadataKey(metadataKeys: List<Long>, deliveryType: DeliveryTypes, accountId: Long) {
        if (metadataKeys.isNotEmpty()) {
            db.emailDao().changeDeliveryTypeByMetadataKey(metadataKeys, deliveryType,
                    listOf(DeliveryTypes.getTrueOrdinal(DeliveryTypes.UNSEND),
                    DeliveryTypes.getTrueOrdinal(DeliveryTypes.NONE)), accountId)
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

    fun updateDeleteLabel(uuid: String, accountId: Long) {
        db.labelDao().deleteByLabelUUID(
                uuid = uuid,
                accountId = accountId
        )
    }

    fun updateDeleteThreadPermanently(threadIds: List<String>, activeAccount: ActiveAccount) {
        if(threadIds.isNotEmpty()){
            db.emailDao().getEmailsFromThreadIds(threadIds, activeAccount.id).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = activeAccount.recipientId,
                        domain = activeAccount.domain)
            }
            db.emailDao().deleteThreads(threadIds, listOf(Label.defaultItems.trash.id, Label.defaultItems.spam.id), activeAccount.id)
        }
    }

    fun updateDeleteEmailPermanently(metadataKeys: List<Long>, activeAccount: ActiveAccount) {
        if(metadataKeys.isNotEmpty()){
            val emails = db.emailDao().getAllEmailsByMetadataKey(metadataKeys, activeAccount.id)
            if(emails.isEmpty()) return
            emails.forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = activeAccount.recipientId,
                        domain = activeAccount.domain)
            }
            db.emailDao().deleteAll(emails)
        }
    }

    fun updateDeleteEmailPermanentlyByIds(emailIds: List<Long>, activeAccount: ActiveAccount) {
        if(emailIds.isNotEmpty()){
            val emails = db.emailDao().getAllEmailsbyId(emailIds, activeAccount.id)
            if(emails.isEmpty()) return
            emails.forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = activeAccount.recipientId,
                        domain = activeAccount.domain)
            }
            db.emailDao().deleteAll(emails)
        }
    }

    fun updateThreadLabels(threadIds: List<String>, labelsAdded: List<String>, labelsRemoved: List<String>, accountId: Long) {
        if(threadIds.isNotEmpty()){

            val systemLabels = db.labelDao().get(Label.defaultItems.toList()
                    .filter { it.text !in listOf(Label.LABEL_SPAM, Label.LABEL_TRASH, Label.LABEL_STARRED) }
                    .map { it.text }, accountId)
            val threads = db.emailDao().getEmailsFromThreadIds(threadIds, accountId)
            if(threads.isEmpty()) return
            val emailIds = threads.map { it.id }
            val removedLabels = db.labelDao().get(labelsRemoved, accountId)
            val removedNonSystemLabelIds = removedLabels.filter { !systemLabels.contains(it) }.map { it.id }
            val addedLabels = db.labelDao().get(labelsAdded, accountId)

            db.emailLabelDao().deleteRelationByLabelsAndEmailIds(removedNonSystemLabelIds, emailIds)

            if(Label.defaultItems.trash in removedLabels){
                db.emailLabelDao().deleteRelationByLabelsAndEmailIds(listOf(Label.defaultItems.trash.id), emailIds)
            }

            if(Label.defaultItems.trash in addedLabels){
                db.emailDao().updateEmailTrashDate(Date(), emailIds, accountId)
            }

            val account = ActiveAccount.loadFromDB(db.accountDao().getAccountById(accountId)!!)!!

            if(Label.defaultItems.spam in addedLabels){
                db.contactDao().uptickSpamCounter(threads.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }.filter { it != account.userEmail }, accountId)
            }

            if(Label.defaultItems.spam in removedLabels){
                db.contactDao().resetSpamCounter(threads.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }.filter { it != account.userEmail }, accountId)
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

    fun updateEmailLabels(metadataKeys: List<Long>, labelsAdded: List<String>, labelsRemoved: List<String>, accountId: Long) {
        if(metadataKeys.isNotEmpty()){

            val systemLabels = db.labelDao().get(Label.defaultItems.toList()
                    .filter { it.text !in listOf(Label.LABEL_SPAM, Label.LABEL_TRASH, Label.LABEL_STARRED) }
                    .map { it.text }, accountId)
            val emails = db.emailDao().getAllEmailsByMetadataKey(metadataKeys, accountId)
            if(emails.isEmpty()) return
            val emailIds = emails.map { it.id }
            val removedLabels = db.labelDao().get(labelsRemoved, accountId)
            val removedNonSystemLabelIds = removedLabels.filter { !systemLabels.contains(it) }.map { it.id }
            val addedLabels = db.labelDao().get(labelsAdded, accountId)

            db.emailLabelDao().deleteRelationByLabelsAndEmailIds(removedNonSystemLabelIds, emailIds)

            if(Label.defaultItems.trash in removedLabels){
                db.emailLabelDao().deleteRelationByLabelsAndEmailIds(listOf(Label.defaultItems.trash.id), emailIds)
            }

            if(Label.defaultItems.trash in addedLabels){
                db.emailDao().updateEmailTrashDate(Date(), emailIds, accountId)
            }

            val account = ActiveAccount.loadFromDB(db.accountDao().getAccountById(accountId)!!)!!

            if(Label.defaultItems.spam in addedLabels){
                db.contactDao().uptickSpamCounter(emails.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }.filter { it != account.userEmail }, accountId)
            }

            if(Label.defaultItems.spam in removedLabels){
                db.contactDao().resetSpamCounter(emails.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }.filter { it != account.userEmail }, accountId)
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

    fun updateUserName(recipientId: String, domain: String, name: String, accountId: Long) {
        db.contactDao().updateContactName("$recipientId@$domain", name, accountId)
        db.accountDao().updateProfileName(name, recipientId, domain)
    }

    fun updateUnsendStatusByMetadataKey(metadataKey: Long, unsentDate: Date, activeAccount: ActiveAccount) {
        db.emailDao().changeDeliveryTypeByMetadataKey(metadataKey, DeliveryTypes.UNSEND, activeAccount.id)
        db.emailDao().unsendEmailByMetadataKey(metadataKey, "", "",
                unsentDate, activeAccount.id)
        db.fileDao().changeFileStatusByEmailid(db.emailDao().getEmailByMetadataKey(metadataKey, activeAccount.id).id, 0)
        EmailUtils.deleteEmailInFileSystem(
                filesDir = filesDir,
                metadataKey = metadataKey,
                recipientId = activeAccount.recipientId,
                domain = activeAccount.domain
        )
    }

    fun updateUnreadStatusByThreadId(emailThreads: List<String>, updateUnreadStatus: Boolean, accountId: Long) {
        db.emailDao().toggleReadByThreadId(emailThreads, updateUnreadStatus, accountId)
    }

    fun updateUnreadStatusByMetadataKeys(metadataKeys: List<Long>, updateUnreadStatus: Boolean, accountId: Long) {
        db.emailDao().toggleReadByMetadataKey(metadataKeys, updateUnreadStatus, accountId)
    }

    fun insertIncomingEmail(signalClient: SignalClient, apiClient: EmailInsertionAPIClient,
                                     metadata: EmailMetadata, activeAccount: ActiveAccount) {
        EmailInsertionSetup.insertIncomingEmailTransaction(signalClient = signalClient,
                dao = db.emailInsertionDao(), apiClient = apiClient, metadata = metadata,
                activeAccount = activeAccount, filesDir = filesDir)
    }

    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String, activeAccount: ActiveAccount): EmailThread {

        val id = email.id
        val labels = db.emailLabelDao().getLabelsFromEmail(id)
        val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
        val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
        val files = db.fileDao().getAttachmentsFromEmail(id)
        val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)
        email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

        val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels, activeAccount.id)
        var totalFiles = 0
        val headerData = mutableListOf<EmailThread.HeaderData>()
        val participants = emails.flatMap {
            val contacts = mutableListOf<Contact>()
            val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
            if(selectedLabel == Label.defaultItems.sent.text){
                if(EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)){
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                }
            }
            else{
                val fromContact = ContactUtils.getFromContact(db.emailContactDao(), db.contactDao(),
                        activeAccount.id, it.id, it.fromAddress)
                contacts.addAll(listOf(fromContact))
                contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM)
                        .filter { contact -> contact.id != fromContact.id })
            }
            contacts.forEach { contact ->
                when {
                    EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_DRAFT) -> headerData.add(EmailThread.HeaderData(
                            name = EmailUtils.DRAFT_HEADER_PLACEHOLDER,
                            isDraft = true,
                            isMe = false,
                            isUnread = it.unread
                    ))
                    contact.email == userEmail -> headerData.add(EmailThread.HeaderData(
                            name = contact.name,
                            isDraft = false,
                            isMe = true,
                            isUnread = it.unread
                    ))
                    else -> headerData.add(EmailThread.HeaderData(
                            name = contact.name,
                            isDraft = false,
                            isMe = false,
                            isUnread = it.unread
                    ))
                }
            }
            totalFiles += db.fileDao().getAttachmentsFromEmail(it.id).size
            contacts
        }

        val fromContact = ContactUtils.getFromContact(db.emailContactDao(), db.contactDao(),
                activeAccount.id, email.id, email.fromAddress)
        val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                email.metadataKey, email.content,
                activeAccount.recipientId, activeAccount.domain)

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
                allFilesAreInline = files.filter { it.cid != null && it.cid != "" }.size == totalFiles,
                headerData = headerData.distinctBy { it.name }
        )
    }

    fun getThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                            startDate: Date?, limit: Int,
                                            rejectedLabels: List<Label>, activeAccount: ActiveAccount): List<EmailThread> {
        val labels = db.labelDao().getAll(activeAccount.id)
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
                    accountId = activeAccount.id)

        else
            db.emailDao().getInitialEmailThreadsFromMailboxLabel(
                    isTrashOrSpam = (selectedLabel in conditionalLabels),
                    rejectedLabels = rejectedIdLabels,
                    selectedLabel = selectedLabel,
                    limit = limit,
                    accountId = activeAccount.id)

        emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                filesDir, it.metadataKey, it.content,
                activeAccount.recipientId, activeAccount.domain).first) }

        return emails.map { email ->
            getEmailThreadFromEmail(email, labelName,
                    Label.defaultItems.rejectedLabelsByMailbox(
                            db.labelDao().get(labelName, activeAccount.id)
                    ).map { it.id }, userEmail, activeAccount)
        } as ArrayList<EmailThread>
    }

    fun getEmailIdsFromTrashExpiredEmails(accountId: Long): List<Long>{
        val labelId = Label.defaultItems.trash.id
        return db.emailDao().getTrashExpiredEmailIds(labelId, accountId)
    }

}