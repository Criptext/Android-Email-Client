package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.composer.Validator
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailThreadValidator
import com.criptext.mail.utils.EmailUtils
import java.io.File
import java.util.*

/**
 * Created by sebas on 1/26/18.
 */

interface MailboxLocalDB {

    fun createLabelEmailRelations(emailLabels: List<EmailLabel>)
    fun updateUnreadStatus(emailThreads: List<String>,
                           updateUnreadStatus: Boolean,
                           rejectedLabels: List<Long>, accountId: Long)
    fun getCustomLabels(accountId: Long): List<Label>
    fun getCustomAndVisibleLabels(accountId: Long): List<Label>
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long, accountId: Long)
    fun getThreadsFromMailboxLabel(
            userEmail: String,
            filterUnread: Boolean,
            labelName: String,
            startDate: Date?,
            limit: Int,
            rejectedLabels: List<Label>,
            activeAccount: ActiveAccount): List<EmailThread>
    fun getNewThreadsFromMailboxLabel(
            userEmail: String,
            labelName: String,
            mostRecentDate: Date?,
            rejectedLabels: List<Label>,
            activeAccount: ActiveAccount): List<EmailThread>
    fun getLabelsFromLabelType(labelNames: List<String>, accountId: Long): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getLabelByName(labelName: String, accountId: Long): Label
    fun getLabelsByName(labelName: List<String>, accountId: Long): List<Label>
    fun getLabelsById(ids: List<Long>, accountId: Long): List<Label>
    fun getLabelById(id: Long, accountId: Long): Label?
    fun updateEmailAndAddLabel(id: Long, threadId : String, messageId: String, isSecure: Boolean,
                               metadataKey: Long, date: Date, status: DeliveryTypes, accountId: Long)
    fun updateDeliveryType(id: Long, status: DeliveryTypes, accountId: Long)
    fun getExistingAccount(): Account?
    fun setActiveAccount(id: Long)
    fun getLoggedAccounts(): List<Account>
    fun getUnreadCounterLabel(labelId: Long, accountId: Long): Int
    fun setTrashDate(emailIds: List<Long>, accountId: Long)
    fun getTotalCounterLabel(labelId: Long, accountId: Long): Int
    fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>, accountId: Long): List<Email>
    fun getEmailById(id: Long, accountId: Long): Email?
    fun getFullEmailById(emailId: Long, account: ActiveAccount): FullEmail?
    fun getPendingEmails(deliveryTypes: List<Int>, account: ActiveAccount): List<FullEmail>
    fun deleteThreads(threadIds: List<String>, activeAccount: ActiveAccount)
    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String, activeAccount: ActiveAccount): EmailThread
    fun getEmailThreadFromId(threadId: String, selectedLabel: String,
                                rejectedLabels: List<Long>, userEmail: String, activeAccount: ActiveAccount): EmailThread
    fun getThreadsIdsFromLabel(labelName: String, accountId: Long): List<String>
    fun getEmailMetadataKeysFromLabel(labelName: String, accountId: Long): List<Long>
    fun getExternalData(id: Long): EmailExternalSession?
    fun saveExternalSession(externalSession: EmailExternalSession)
    fun deleteEmail(emailId: List<Long>, accountId: Long)
    fun fileNeedsDuplicate(id: Long): Boolean
    fun updateFileToken(id: Long, newToken: String)
    fun getFileKeyByFileId(id: Long): String?
    fun increaseContactScore(emailIds: List<Long>)
    fun getAccountById(accountId: Long): Account?
    fun updateSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String>
    fun resetSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String>
    fun updateIsTrusted(emailAddresses: List<String>, isTrusted: Boolean)
    fun getAlias(aliasEmail: String?): Alias?


    class Default(private val db: AppDatabase, private val filesDir: File): MailboxLocalDB {

        override fun updateIsTrusted(emailAddresses: List<String>, isTrusted: Boolean) {
            db.contactDao().updateContactsIsTrusted(emailAddresses, isTrusted)
        }

        override fun getAlias(aliasEmail: String?): Alias? {
            aliasEmail ?: return null
            val domain = EmailAddressUtils.extractEmailAddressDomain(aliasEmail)
            val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(aliasEmail, domain)
            return if(domain == Contact.mainDomain){
                db.aliasDao().getCriptextAliasByName(recipientId)
            } else {
                db.aliasDao().getAliasByName(recipientId, domain)
            }
        }

        override fun resetSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String> {
            val emails = db.emailDao().getAllEmailsbyId(emailIds, accountId)
            val fromContacts = emails.filter { !it.fromAddress.contains(userEmail) }.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }
            if(fromContacts.isNotEmpty())
                db.contactDao().resetSpamCounter(fromContacts, accountId)
            return fromContacts
        }

        override fun updateSpamCounter(emailIds: List<Long>, accountId: Long, userEmail: String): List<String> {
            val emails = db.emailDao().getAllEmailsbyId(emailIds, accountId)
            val fromContacts = emails.filter { !it.fromAddress.contains(userEmail) }.map { EmailAddressUtils.extractEmailAddress(it.fromAddress) }
            if(fromContacts.isNotEmpty())
                db.contactDao().uptickSpamCounter(fromContacts, accountId)
            return fromContacts
        }

        override fun getAccountById(accountId: Long): Account? {
            return db.accountDao().getAccountById(accountId)
        }

        override fun increaseContactScore(emailIds: List<Long>) {
            db.emailContactDao().increaseScore(emailIds, ContactTypes.FROM)
        }

        override fun getFullEmailById(emailId: Long, account: ActiveAccount): FullEmail? {
            val email = db.emailDao().getEmailById(emailId, account.id) ?: return null
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
                    account.recipientId, account.domain)

            return FullEmail(
                        email = email.copy(content = emailContent.first),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO, fileKey = fileKey?.key, headers = emailContent.second)


        }

        override fun getFileKeyByFileId(id: Long): String? {
            val fileKey = db.fileKeyDao().getFileById(id)
            return if(fileKey != null && !fileKey.key.isNullOrEmpty())
                fileKey.key
            else
                null
        }

        override fun updateFileToken(id: Long, newToken: String) {
            db.fileDao().updateToken(id, newToken)
        }

        override fun fileNeedsDuplicate(id: Long): Boolean {
            return db.fileDao().getFileById(id)?.shouldDuplicate ?: false
        }

        override fun getEmailById(id: Long, accountId: Long): Email? {
            return db.emailDao().getEmailById(id, accountId)
        }

        override fun saveExternalSession(externalSession: EmailExternalSession) {
            db.emailExternalSessionDao().insert(externalSession)
        }

        override fun getExternalData(id: Long): EmailExternalSession? {
            return db.emailExternalSessionDao().getExternalSessionByEmailId(id)
        }

        override fun getPendingEmails(deliveryTypes: List<Int>, account: ActiveAccount): List<FullEmail> {
            val emails = db.emailDao().getPendingEmails(deliveryTypes,
                    Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox).map { it.id }, account.id)
            val fullEmails =  emails.map {
                val id = it.id
                val labels = db.emailLabelDao().getLabelsFromEmail(id)
                val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
                val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
                val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
                val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
                val files = db.fileDao().getAttachmentsFromEmail(id)
                val fileKey = db.fileKeyDao().getAttachmentKeyFromEmail(id)

                val emailContent =  EmailUtils.getEmailContentFromFileSystem(filesDir,
                        it.metadataKey, it.content,
                        account.recipientId, account.domain)

                FullEmail(
                        email = it.copy(content = emailContent.first),
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactsFROM[0],
                        files = files,
                        labels = labels,
                        to = contactsTO, fileKey = fileKey?.key, headers = emailContent.second)
            }

            fullEmails.lastOrNull()?.viewOpen = true

            return fullEmails
        }

        override fun setTrashDate(emailIds: List<Long>, accountId: Long) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds, accountId)
        }

        override fun getThreadsIdsFromLabel(labelName: String, accountId: Long): List<String> {
            val labelId = db.labelDao().get(labelName, accountId).id
            return db.emailDao().getThreadIdsFromLabel(labelId, accountId)
        }

        override fun getEmailMetadataKeysFromLabel(labelName: String, accountId: Long): List<Long> {
            val labelId = db.labelDao().get(labelName, accountId).id
            return db.emailDao().getMetadataKeysFromLabel(labelId, accountId)
        }

        override fun getEmailThreadFromId(threadId: String, selectedLabel: String, rejectedLabels: List<Long>,
                                          userEmail: String, activeAccount: ActiveAccount): EmailThread {

            val email = db.emailDao().getEmailsFromThreadIds(listOf(threadId), activeAccount.id).last()
            return emailThread(email, rejectedLabels, selectedLabel, userEmail, activeAccount)
        }

        override fun createLabelsForEmailInbox(insertedEmailId: Long, accountId: Long) {
            val labelInbox = db.labelDao().get(Label.LABEL_INBOX, accountId)
            db.emailLabelDao().insert(EmailLabel(
                    labelId = labelInbox.id,
                    emailId = insertedEmailId))
        }

        override fun addEmail(email: Email): Long {
            return db.emailDao().insert(email)
        }

        override fun getCustomLabels(accountId: Long): List<Label>{
            return db.labelDao().getAllCustomLabels(accountId)
        }

        override fun getCustomAndVisibleLabels(accountId: Long): List<Label> {
            return db.labelDao().getCustomAndVisibleLabels(accountId)
        }

        override fun getLabelsFromThreadIds(threadIds: List<String>) : List<Label> {
            val labelSet = HashSet<Label>()
            threadIds.forEach {
                val labels = db.emailLabelDao().getLabelsFromEmailThreadId(it)
                labelSet.addAll(labels)
            }
            return labelSet.toList()
        }

        override fun createLabelEmailRelations(emailLabels: List<EmailLabel>){
            return db.emailLabelDao().insertAll(emailLabels)
        }

        private fun createLabelEmailSent(emailId: Long){
            db.emailLabelDao().insert(EmailLabel(
                    labelId = Label.defaultItems.sent.id,
                    emailId = emailId))
        }

        private fun createLabelEmailInbox(emailId: Long){
            db.emailLabelDao().insert(EmailLabel(
                    labelId = Label.defaultItems.inbox.id,
                    emailId = emailId))
        }

        override fun updateUnreadStatus(emailThreads: List<String>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>, accountId: Long) {
            emailThreads.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it, rejectedLabels, accountId)
                        .map { email -> email.id }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus, accountId = accountId)
            }
        }

        override fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                            rejectedLabels: List<Long>, userEmail: String, activeAccount: ActiveAccount): EmailThread {

            return emailThread(email, rejectedLabels, selectedLabel, userEmail, activeAccount)
        }

        override fun getThreadsFromMailboxLabel(userEmail: String, filterUnread: Boolean, labelName: String,
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
            var emails = if(startDate != null) {
                if(filterUnread)
                    db.emailDao().getEmailThreadsFromMailboxLabelFiltered(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
                            startDate = startDate,
                            rejectedLabels = rejectedIdLabels,
                            selectedLabel = selectedLabel,
                            limit = limit,
                            accountId = activeAccount.id)
                else
                    db.emailDao().getEmailThreadsFromMailboxLabel(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
                            startDate = startDate,
                            rejectedLabels = rejectedIdLabels,
                            selectedLabel = selectedLabel,
                            limit = limit,
                            accountId =  activeAccount.id)
            } else {
                if (filterUnread)
                    db.emailDao().getInitialEmailThreadsFromMailboxLabelFiltered(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
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
            }

            emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                    filesDir, it.metadataKey, it.content,
                    activeAccount.recipientId, activeAccount.domain).first) }

            return if (emails.isNotEmpty()){
                emails.map { email ->
                    getEmailThreadFromEmail(email, labelName,
                            Label.defaultItems.rejectedLabelsByMailbox(
                                    db.labelDao().get(labelName, activeAccount.id)
                            ).map { it.id }, userEmail, activeAccount)
                } as ArrayList<EmailThread>
            }else emptyList()
        }

        override fun getNewThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                                mostRecentDate: Date?,
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
            var emails = if(mostRecentDate != null)
                db.emailDao().getNewEmailThreadsFromMailboxLabel(
                        isTrashOrSpam = (selectedLabel in conditionalLabels),
                        startDate = mostRecentDate,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        accountId = activeAccount.id)

            else
                emptyList()

            emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                    filesDir,
                    it.metadataKey, it.content,
                    activeAccount.recipientId, activeAccount.domain).first) }

            return if (emails.isNotEmpty()){
                emails.map { email ->
                    getEmailThreadFromEmail(email, labelName,
                            Label.defaultItems.rejectedLabelsByMailbox(
                                    db.labelDao().get(labelName, activeAccount.id)
                            ).map { it.id }, userEmail, activeAccount)
                } as ArrayList<EmailThread>
            }else emptyList()
        }

        override fun getLabelsFromLabelType(labelNames: List<String>, accountId: Long): List<Label> {
            return db.labelDao().get(labelNames, accountId)
        }

        override fun getLabelByName(labelName: String, accountId: Long): Label {
            return db.labelDao().get(labelName, accountId)
        }

        override fun getLabelsByName(labelName: List<String>, accountId: Long): List<Label> {
            return db.labelDao().get(labelName, accountId)
        }

        override fun getLabelsById(ids: List<Long>, accountId: Long): List<Label> {
            return db.labelDao().getById(ids, accountId)
        }

        override fun getLabelById(id: Long, accountId: Long): Label? {
            return db.labelDao().getById(id, accountId)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: List<Long>, accountId: Long) {
            db.emailDao().deleteByIds(emailId, accountId)
        }

        private fun updateEmail(id: Long, threadId: String, messageId : String, metadataKey: Long,
                        date: Date, status: DeliveryTypes, accountId: Long, isSecure: Boolean) {
            db.emailDao().updateEmail(id = id, threadId = threadId, messageId = messageId,
                    metadataKey = metadataKey, date = date, status = status, accountId = accountId,
                    isSecure = isSecure)
        }

        override fun updateEmailAndAddLabel(id: Long, threadId: String, messageId: String, isSecure: Boolean,
                                            metadataKey: Long, date: Date, status: DeliveryTypes,
                                            accountId: Long) {
            db.runInTransaction {
                updateEmail(id = id, threadId = threadId, messageId = messageId, isSecure = isSecure,
                        metadataKey = metadataKey, date = date, status = status, accountId = accountId)
                deleteRelationByEmailIds(arrayListOf(id))
                createLabelEmailSent(id)
                if(status == DeliveryTypes.DELIVERED)
                    createLabelEmailInbox(id)
            }
        }

        override fun updateDeliveryType(id: Long, status: DeliveryTypes, accountId: Long) {
            db.emailDao().updateEmailStatus(id, status, accountId)
        }

        override fun getExistingAccount(): Account? {
            return db.accountDao().getLoggedInAccount()
        }

        override fun setActiveAccount(id: Long) {
            db.accountDao().updateActiveInAccount()
            db.accountDao().updateActiveInAccount(id)
        }

        override fun getLoggedAccounts(): List<Account> {
            return db.accountDao().getLoggedInAccounts()
        }

        override fun getUnreadCounterLabel(labelId: Long, accountId: Long): Int {
            val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(
                    db.labelDao().getLabelById(labelId, accountId)).map { it.id }
            return db.emailDao().getTotalUnreadThreads(rejectedLabels, "%L${labelId}L%", accountId).size
        }

        override fun getTotalCounterLabel(labelId: Long,accountId: Long): Int {
            return db.emailDao().getTotalThreads("%L${labelId}L%", accountId).size
        }

        override fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>, accountId: Long): List<Email> {
            return db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels, accountId)
        }

        override fun deleteThreads(threadIds: List<String>, activeAccount: ActiveAccount) {
            db.emailDao().getEmailsFromThreadIds(threadIds, activeAccount.id).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = activeAccount.recipientId,
                        domain = activeAccount.domain)
            }
            db.emailDao().deleteThreads(threadIds, activeAccount.id)
        }

        private fun emailThread(email: Email, rejectedLabels: List<Long>, selectedLabel: String,
                                userEmail: String, activeAccount: ActiveAccount): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val emailsInSelectedLabel = if(selectedLabel != Label.LABEL_ALL_MAIL)
                db.emailLabelDao().getEmailCountInLabelByEmailId(email.threadId,
                        db.labelDao().get(selectedLabel, activeAccount.id).id) else -1
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val fileKey: FileKey? = db.fileKeyDao().getAttachmentKeyFromEmail(id)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

            val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels, activeAccount.id)
            var totalFiles = 0
            val headerData = mutableListOf<EmailThread.HeaderData>()
            val participants = emails.flatMap {
                val contacts = mutableListOf<Contact>()
                val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                if (selectedLabel == Label.defaultItems.sent.text) {
                    if (EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)) {
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                    }
                } else {
                    val fromContact = ContactUtils.getFromContact(db.emailContactDao(), db.contactDao(),
                            activeAccount.id, it.id, it.fromAddress)
                    contacts.addAll(listOf(fromContact))
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM)
                            .filter { contact -> contact.id != fromContact.id })
                }
                contacts.map { contact ->
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
                            email = email.copy(
                                    content = emailContent.first),
                            bcc = contactsBCC,
                            cc = contactsCC,
                            from = fromContact,
                            files = files,
                            labels = labels,
                            to = contactsTO,
                            fileKey = fileKey?.key,
                            headers = emailContent.second),
                    totalEmails = getEmailCount(emailsInSelectedLabel, emails.size, selectedLabel),
                    hasFiles = totalFiles > 0,
                    allFilesAreInline = files.filter { it.cid != null && it.cid != "" }.size == totalFiles,
                    headerData = headerData.distinctBy { it.name }
            )
        }

        private fun getEmailCount(emailsInSelectedLabel: Int, emailsSize: Int,
                                  selectedLabel: String): Int{
            return if(selectedLabel == Label.LABEL_TRASH || selectedLabel == Label.LABEL_SPAM )
                emailsInSelectedLabel
            else
                emailsSize
        }
    }

}
