package com.criptext.mail.db

import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailThread
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
                           rejectedLabels: List<Long>)
    fun getCustomLabels(): List<Label>
    fun getCustomAndVisibleLabels(): List<Label>
    fun getLabelsFromThreadIds(threadIds: List<String>): List<Label>
    fun addEmail(email: Email) : Long
    fun createLabelsForEmailInbox(insertedEmailId: Long)
    fun getThreadsFromMailboxLabel(
            userEmail: String,
            filterUnread: Boolean,
            labelName: String,
            startDate: Date?,
            limit: Int,
            rejectedLabels: List<Label>): List<EmailThread>
    fun getNewThreadsFromMailboxLabel(
            userEmail: String,
            labelName: String,
            mostRecentDate: Date?,
            rejectedLabels: List<Label>): List<EmailThread>
    fun getLabelsFromLabelType(labelNames: List<String>): List<Label>
    fun deleteRelationByEmailIds(emailIds: List<Long>)
    fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>)
    fun getLabelByName(labelName: String): Label
    fun getLabelsByName(labelName: List<String>): List<Label>
    fun updateEmailAndAddLabel(id: Long, threadId : String, messageId: String,
                               metadataKey: Long, date: Date, status: DeliveryTypes)
    fun updateDeliveryType(id: Long, status: DeliveryTypes, accountId: Long)
    fun getExistingAccount(): Account?
    fun setActiveAccount(id: Long)
    fun getLoggedAccounts(): List<Account>
    fun getUnreadCounterLabel(labelId: Long): Int
    fun setTrashDate(emailIds: List<Long>)
    fun getTotalCounterLabel(labelId: Long): Int
    fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email>
    fun getEmailById(id: Long, accountId: Long): Email?
    fun getFullEmailById(emailId: Long, accountId: Long): FullEmail?
    fun getPendingEmails(deliveryTypes: List<Int>): List<FullEmail>
    fun deleteThreads(threadIds: List<String>)
    fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                         rejectedLabels: List<Long>, userEmail: String): EmailThread
    fun getEmailThreadFromId(threadId: String, selectedLabel: String,
                                rejectedLabels: List<Long>, userEmail: String): EmailThread
    fun getThreadsIdsFromLabel(labelName: String): List<String>
    fun getEmailMetadataKeysFromLabel(labelName: String): List<Long>
    fun getExternalData(id: Long): EmailExternalSession?
    fun saveExternalSession(externalSession: EmailExternalSession)
    fun deleteEmail(emailId: List<Long>)
    fun fileNeedsDuplicate(id: Long): Boolean
    fun updateFileToken(id: Long, newToken: String)
    fun getFileKeyByFileId(id: Long): String?
    fun increaseContactScore(emailIds: List<Long>)


    class Default(private val db: AppDatabase, private val filesDir: File): MailboxLocalDB {

        override fun increaseContactScore(emailIds: List<Long>) {
            db.emailContactDao().increaseScore(emailIds, ContactTypes.FROM)
        }

        override fun getFullEmailById(emailId: Long, accountId: Long): FullEmail? {
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

        override fun getPendingEmails(deliveryTypes: List<Int>): List<FullEmail> {
            val emails = db.emailDao().getPendingEmails(deliveryTypes,
                    Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox).map { it.id }, getExistingAccount()!!.id)
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
                        db.accountDao().getLoggedInAccount()!!.recipientId)

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

        override fun setTrashDate(emailIds: List<Long>) {
            db.emailDao().updateEmailTrashDate(Date(), emailIds, getExistingAccount()!!.id)
        }

        override fun getThreadsIdsFromLabel(labelName: String): List<String> {
            val labelId = db.labelDao().get(labelName, getExistingAccount()!!.id).id
            return db.emailDao().getThreadIdsFromLabel(labelId, getExistingAccount()!!.id)
        }

        override fun getEmailMetadataKeysFromLabel(labelName: String): List<Long> {
            val labelId = db.labelDao().get(labelName, getExistingAccount()!!.id).id
            return db.emailDao().getMetadataKeysFromLabel(labelId, getExistingAccount()!!.id)
        }

        override fun getEmailThreadFromId(threadId: String, selectedLabel: String, rejectedLabels: List<Long>, userEmail: String): EmailThread {

            val email = db.emailDao().getEmailsFromThreadIds(listOf(threadId), getExistingAccount()!!.id).last()
            return emailThread(email, rejectedLabels, selectedLabel, userEmail)
        }

        override fun createLabelsForEmailInbox(insertedEmailId: Long) {
            val labelInbox = db.labelDao().get(Label.LABEL_INBOX, getExistingAccount()!!.id)
            db.emailLabelDao().insert(EmailLabel(
                    labelId = labelInbox.id,
                    emailId = insertedEmailId))
        }

        override fun addEmail(email: Email): Long {
            return db.emailDao().insert(email)
        }

        override fun getCustomLabels(): List<Label>{
            return db.labelDao().getAllCustomLabels(getExistingAccount()!!.id)
        }

        override fun getCustomAndVisibleLabels(): List<Label> {
            return db.labelDao().getCustomAndVisibleLabels(getExistingAccount()!!.id)
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

        override fun updateUnreadStatus(threadIds: List<String>,
                                        updateUnreadStatus: Boolean,
                                        rejectedLabels: List<Long>) {
            threadIds.forEach {
                val emailsIds = db.emailDao().getEmailsFromThreadId(it, rejectedLabels, getExistingAccount()!!.id)
                        .map { email -> email.id }
                db.emailDao().toggleRead(ids = emailsIds, unread = updateUnreadStatus, accountId = getExistingAccount()!!.id)
            }
        }

        override fun getEmailThreadFromEmail(email: Email, selectedLabel: String,
                                            rejectedLabels: List<Long>, userEmail: String): EmailThread {

            return emailThread(email, rejectedLabels, selectedLabel, userEmail)
        }

        override fun getThreadsFromMailboxLabel(userEmail: String, filterUnread: Boolean, labelName: String,
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
            var emails = if(startDate != null) {
                if(filterUnread)
                    db.emailDao().getEmailThreadsFromMailboxLabelFiltered(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
                            startDate = startDate,
                            rejectedLabels = rejectedIdLabels,
                            selectedLabel = selectedLabel,
                            limit = limit,
                            accountId = account.id)
                else
                    db.emailDao().getEmailThreadsFromMailboxLabel(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
                            startDate = startDate,
                            rejectedLabels = rejectedIdLabels,
                            selectedLabel = selectedLabel,
                            limit = limit,
                            accountId =  account.id)
            } else {
                if (filterUnread)
                    db.emailDao().getInitialEmailThreadsFromMailboxLabelFiltered(
                            isTrashOrSpam = (selectedLabel in conditionalLabels),
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
            }

            emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                    filesDir, it.metadataKey, it.content,
                    db.accountDao().getLoggedInAccount()!!.recipientId).first) }

            return if (emails.isNotEmpty()){
                emails.map { email ->
                    getEmailThreadFromEmail(email, labelName,
                            Label.defaultItems.rejectedLabelsByMailbox(
                                    db.labelDao().get(labelName, account.id)
                            ).map { it.id }, userEmail)
                } as ArrayList<EmailThread>
            }else emptyList()
        }

        override fun getNewThreadsFromMailboxLabel(userEmail: String, labelName: String,
                                                mostRecentDate: Date?,
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
            var emails = if(mostRecentDate != null)
                db.emailDao().getNewEmailThreadsFromMailboxLabel(
                        isTrashOrSpam = (selectedLabel in conditionalLabels),
                        startDate = mostRecentDate,
                        rejectedLabels = rejectedIdLabels,
                        selectedLabel = selectedLabel,
                        accountId = account.id)

            else
                emptyList()

            emails = emails.map { it.copy(content = EmailUtils.getEmailContentFromFileSystem(
                    filesDir,
                    it.metadataKey, it.content,
                    db.accountDao().getLoggedInAccount()!!.recipientId).first) }

            return if (emails.isNotEmpty()){
                emails.map { email ->
                    getEmailThreadFromEmail(email, labelName,
                            Label.defaultItems.rejectedLabelsByMailbox(
                                    db.labelDao().get(labelName, account.id)
                            ).map { it.id }, userEmail)
                } as ArrayList<EmailThread>
            }else emptyList()
        }

        override fun getLabelsFromLabelType(labelNames: List<String>): List<Label> {
            return db.labelDao().get(labelNames, getExistingAccount()!!.id)
        }

        override fun getLabelByName(labelName: String): Label {
            return db.labelDao().get(labelName, getExistingAccount()!!.id)
        }

        override fun getLabelsByName(labelName: List<String>): List<Label> {
            return db.labelDao().get(labelName, getExistingAccount()!!.id)
        }

        override fun deleteRelationByEmailIds(emailIds: List<Long>) {
            db.emailLabelDao().deleteRelationByEmailIds(emailIds)
        }

        override fun deleteRelationByLabelAndEmailIds(labelId: Long, emailIds: List<Long>){
            db.emailLabelDao().deleteRelationByLabelAndEmailIds(labelId, emailIds)
        }

        override fun deleteEmail(emailId: List<Long>) {
            db.emailDao().deleteByIds(emailId, getExistingAccount()!!.id)
        }

        private fun updateEmail(id: Long, threadId: String, messageId : String, metadataKey: Long,
                        date: Date, status: DeliveryTypes) {
            db.emailDao().updateEmail(id = id, threadId = threadId, messageId = messageId,
                    metadataKey = metadataKey, date = date, status = status, accountId = getExistingAccount()!!.id)
        }

        override fun updateEmailAndAddLabel(id: Long, threadId: String, messageId: String,
                                            metadataKey: Long, date: Date, status: DeliveryTypes) {
            db.runInTransaction {
                updateEmail(id = id, threadId = threadId, messageId = messageId,
                        metadataKey = metadataKey, date = date, status = status)
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

        override fun getUnreadCounterLabel(labelId: Long): Int {
            val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(
                    db.labelDao().getLabelById(labelId, getExistingAccount()!!.id)).map { it.id }
            return db.emailDao().getTotalUnreadThreads(rejectedLabels, "%$labelId%", getExistingAccount()!!.id).size
        }

        override fun getTotalCounterLabel(labelId: Long): Int {
            return db.emailDao().getTotalThreads("%$labelId%", getExistingAccount()!!.id).size
        }

        override fun getEmailsByThreadId(threadId: String, rejectedLabels: List<Long>): List<Email> {
            return db.emailDao().getEmailsFromThreadId(threadId, rejectedLabels, getExistingAccount()!!.id)
        }

        override fun deleteThreads(threadIds: List<String>) {
            db.emailDao().getEmailsFromThreadIds(threadIds, getExistingAccount()!!.id).forEach {
                EmailUtils.deleteEmailInFileSystem(
                        filesDir = filesDir,
                        metadataKey = it.metadataKey,
                        recipientId = db.accountDao().getLoggedInAccount()!!.recipientId)
            }
            db.emailDao().deleteThreads(threadIds, getExistingAccount()!!.id)
        }

        private fun emailThread(email: Email, rejectedLabels: List<Long>, selectedLabel: String, userEmail: String): EmailThread {
            val id = email.id
            val labels = db.emailLabelDao().getLabelsFromEmail(id)
            val emailsInSelectedLabel = if(selectedLabel != Label.LABEL_ALL_MAIL)
                db.emailLabelDao().getEmailCountInLabelByEmailId(email.threadId,
                        db.labelDao().get(selectedLabel, getExistingAccount()!!.id).id) else -1
            val contactsCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
            val contactsBCC = db.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
            val contactsFROM = db.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
            val contactsTO = db.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
            val files = db.fileDao().getAttachmentsFromEmail(id)
            val fileKey: FileKey? = db.fileKeyDao().getAttachmentKeyFromEmail(id)
            email.subject = email.subject.replace("^(Re|RE): ".toRegex(), "")
                    .replace("^(Fw|FW|Fwd|FWD): ".toRegex(), "")

            val emails = db.emailDao().getEmailsFromThreadId(email.threadId, rejectedLabels, getExistingAccount()!!.id)
            var totalFiles = 0
            val participants = emails.flatMap {
                val contacts = mutableListOf<Contact>()
                if (selectedLabel == Label.defaultItems.sent.text) {
                    val emailLabels = db.emailLabelDao().getLabelsFromEmail(it.id)
                    if (EmailThreadValidator.isLabelInList(emailLabels, Label.LABEL_SENT)) {
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.TO))
                        contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.CC))
                    }
                } else {
                    contacts.addAll(db.emailContactDao().getContactsFromEmail(it.id, ContactTypes.FROM))
                }
                contacts.map { contact ->
                    if (contact.email == userEmail) {
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
                    id = contactsFROM[0].id,
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
                    allFilesAreInline = files.filter { it.cid != null }.size == totalFiles
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
