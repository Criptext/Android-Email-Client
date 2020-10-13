package com.criptext.mail.scenes.composer.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.composer.workers.SaveEmailWorker
import com.criptext.mail.utils.EmailUtils
import io.mockk.mockk
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SaveEmailWorkerTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var composerLocalDB: ComposerLocalDB
    private lateinit var emailInsertionDao: EmailInsertionDao
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD, blockRemoteContent = true,
            defaultAddress = null)

    private val progressReporter: ProgressReporter<ComposerResult.SaveEmail> = mockk()

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(1, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, "", "criptext.com",
                true, true, type = AccountTypes.STANDARD, blockRemoteContent = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true,
                lastTimeBackup = null, defaultAddress = null))
        composerLocalDB = ComposerLocalDB(db.contactDao(), db.emailDao(), db.fileDao(),
                db.fileKeyDao(), db.labelDao(), db.emailLabelDao(), db.emailContactDao(), db.accountDao(), db.aliasDao(), mActivityRule.activity.filesDir)
        emailInsertionDao = db.emailInsertionDao()
    }

    private fun newWorker(emailId: Long?, threadId: String?, onlySave: Boolean,
                          inputData: ComposerInputData, fileKey: String?): SaveEmailWorker =
            SaveEmailWorker(emailId = emailId, threadId = threadId, composerInputData = inputData,
                    onlySave = onlySave, senderAddress = activeAccount.userEmail, dao = emailInsertionDao,
                    publishFn = {}, attachments = emptyList(), fileKey = fileKey, originalId = null, activeAccount = activeAccount,
                    filesDir = mActivityRule.activity.filesDir, currentLabel = Label.defaultItems.inbox, db = composerLocalDB,
                    goToRecoveryEmail = false)


    @Test
    fun test_should_save_new_composed_email_along_with_new_contacts() {
        val toRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@criptext.com", isTrusted = false, score = 0, spamScore = 0),
                Contact(id = 0, name = "", email = "daniel@criptext.com", isTrusted = false, score = 0, spamScore = 0)
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@criptext.com", score = 0, isTrusted = false, spamScore = 0)
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Email", body = "Hello, this is a test email", fileKey = null, attachments = null,
                fromAddress = activeAccount.userEmail)

        val worker = newWorker(emailId = null, threadId = null, onlySave = false,
                inputData = inputData, fileKey = null)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedEmails = db.emailDao().getAll(activeAccount.id)
        insertedEmails.forEach { it.content = EmailUtils.getEmailContentFromFileSystem(
                mActivityRule.activity.filesDir,
                it.metadataKey,
                it.content,
                activeAccount.recipientId,
                activeAccount.domain).first }
        val insertedContacts = db.contactDao().getAll()
        val insertedContactsAddresses = insertedContacts.map { contact -> contact.email }

        insertedEmails.single().content `shouldEqual` "Hello, this is a test email"
        insertedContactsAddresses `shouldEqual` listOf(
                "tester@criptext.com", "mayer@criptext.com", "daniel@criptext.com", "gianni@criptext.com")
    }

    @Test
    fun test_should_save_a_restored_draft_deleting_the_previous_row() {
        // insert previously generated draft to DB
        val previousDraft = Email(id = 0, messageId = "__MESSAGE_ID__", unread = false,
                content = "This was my original draft", preview = "__PREVIEW__", subject = "draft",
                delivered = DeliveryTypes.NONE, date = Date(), secure = false,
                threadId = "__MESSAGE_ID__", metadataKey = 1246275862L, unsentDate = Date(),
                trashDate = Date(), boundary = null, replyTo = null, fromAddress = "tester@criptext.com",
                accountId = activeAccount.id, isNewsletter = null)
        val draftId = db.emailDao().insert(previousDraft)


        // Now create the finished draft
        val toRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@criptext.com", isTrusted = false, score = 0, spamScore = 0),
                Contact(id = 0, name = "", email = "daniel@criptext.com", score = 0, isTrusted = false, spamScore = 0)
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@criptext.com", isTrusted = false, score = 0, spamScore = 0)
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Finished Draft", body = "Hello, I have finished my draft",
                attachments = null,  fileKey = null, fromAddress = activeAccount.userEmail)

        val worker = newWorker(emailId = draftId, threadId = "__MESSAGE_ID__", onlySave = false,
                inputData = inputData, fileKey = null)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedEmails = db.emailDao().getAll(activeAccount.id)
        insertedEmails.forEach { it.content = EmailUtils.getEmailContentFromFileSystem(
                mActivityRule.activity.filesDir,
                it.metadataKey,
                it.content,
                activeAccount.recipientId,
                activeAccount.domain).first }

        insertedEmails.single().content `shouldEqual` "Hello, I have finished my draft"
    }

    @Test
    fun should_not_touch_already_existing_contacts() {
        // Insert the "already existing" contacts
        val existingRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@criptext.com", isTrusted = false, score = 0, spamScore = 0),
                Contact(id = 0, name = "", email = "daniel@criptext.com", isTrusted = false, score = 0, spamScore = 0)
        )
        db.contactDao().insertAll(existingRecipients)

        // Now create the email and insert it with the existing contacts
        val toRecipients = listOf(
                Contact(id = 1, name = "", email = "mayer@criptext.com", isTrusted = false, score = 0, spamScore = 0),
                Contact(id = 2, name = "", email = "daniel@criptext.com", isTrusted = false, score = 0, spamScore = 0)
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@criptext.com", isTrusted = false, score = 0, spamScore = 0)
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Email", body = "Hello, this is a test email", fileKey = null, attachments = null,
                fromAddress = activeAccount.userEmail)

        val worker = newWorker(emailId = null, threadId = null, onlySave = false,
                inputData = inputData, fileKey = null)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedContacts = db.contactDao().getAll()
        val insertedContactsAddresses = insertedContacts.map { contact -> contact.email }

        insertedContactsAddresses `shouldEqual` listOf(
                "mayer@criptext.com", "daniel@criptext.com", "tester@criptext.com", "gianni@criptext.com")
    }
}