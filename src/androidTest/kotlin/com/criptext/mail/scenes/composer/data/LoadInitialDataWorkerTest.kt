package com.criptext.mail.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import io.mockk.mockk
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by gabriel on 7/2/18.
 */

@RunWith(AndroidJUnit4::class)
class LoadInitialDataWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var composerLocalDB: ComposerLocalDB
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")
    private val testerContact = Contact(email = activeAccount.userEmail, name = "Tester", id = 1)
    private val mayerContact = Contact(email = "mayer@criptext.com", name = "Mayer", id = 2)
    private val danielContact = Contact(email = "daniel@criptext.com", name = "Daniel", id = 3)
    private val emailPreview = EmailPreview(subject = "Test", topText ="Daniel", bodyPreview = "Hola",
            senderName = "Mayer", deliveryStatus = DeliveryTypes.NONE, unread = false, count = 1, timestamp = Date(),
            emailId = 1, threadId = "__THREAD_ID__", isSelected = false, isStarred = false, hasFiles = false, latestEmailUnsentDate = Date(),
            metadataKey = 1)

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())

        db.contactDao().insertAll(listOf(testerContact, mayerContact, danielContact))

        composerLocalDB = ComposerLocalDB(contactDao = db.contactDao(), emailDao = db.emailDao(),
                emailContactDao = db.emailContactDao(), emailLabelDao = db.emailLabelDao(),
                labelDao = db.labelDao(), fileDao = db.fileDao(), accountDao = db.accountDao(),
                fileKeyDao = db.fileKeyDao())

    }

    private fun newWorker(emailId: Long, type: ComposerType): LoadInitialDataWorker =
            LoadInitialDataWorker(db = composerLocalDB, emailId = emailId, composerType = type,
                    userEmailAddress = activeAccount.userEmail, signature = activeAccount.signature,
                    publishFn = {})

    private fun insertEmailToLoad(to: List<Contact>, fromContact: Contact, subject: String,
                                  decryptedBody: String, isDraft: Boolean, fileKey: String?): Long {
        val toCSV = to.map {it.email}.joinToString(separator = ",")
        val metadata = EmailMetadata.DBColumns(to = toCSV,  cc = "", bcc = "",
                    fromContact = fromContact, messageId = "__MESSAGE_ID__",
                    date = "2018-02-21 14:00:00", threadId = "__THREAD_ID__",
                    subject = subject, unread = true, metadataKey = 100L,
                    status = DeliveryTypes.NONE, unsentDate = "2018-02-21 14:00:00")
            val labels = if (isDraft) listOf(Label.defaultItems.inbox)
                        else listOf(Label.defaultItems.draft)

            return EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    decryptedBody = decryptedBody, labels = labels, files = emptyList(), fileKey = fileKey)
    }

    @Test
    fun should_load_a_draft_correctly() {
        val emailId = insertEmailToLoad(to = listOf(mayerContact), fromContact = testerContact,
                subject = "Draft Test", decryptedBody = "Hello this is a draft", isDraft = true, fileKey = null)

        val worker = newWorker(emailId = emailId, type = ComposerType.Draft(draftId = emailId,
                currentLabel = Label.defaultItems.inbox, threadPreview = emailPreview))
        val result = worker.work(mockk()) as ComposerResult.LoadInitialData.Success

        result.initialData.subject `shouldEqual` "Draft Test"
        result.initialData.body `shouldEqual` "Hello this is a draft"
        result.initialData.to `shouldEqual` listOf(mayerContact)
    }

    @Test
    fun should_load_an_email_to_reply_correctly() {
        val emailId = insertEmailToLoad(to = listOf(testerContact), fromContact = mayerContact,
                subject = "Hello", decryptedBody = "Please reply to me.", isDraft = false, fileKey = null)

        val worker = newWorker(emailId = emailId, type = ComposerType.Reply(originalId = emailId,
                currentLabel = Label.defaultItems.inbox, threadPreview = emailPreview))
        val result = worker.work(mockk()) as ComposerResult.LoadInitialData.Success

        result.initialData.subject `shouldEqual` "RE: Hello"
        result.initialData.body `shouldContain` "Please reply to me."
        result.initialData.to `shouldEqual` listOf(mayerContact)
    }

    @Test
    fun should_load_an_email_to_reply_to_all_correctly() {
        val emailId = insertEmailToLoad(to = listOf(testerContact, danielContact),
                fromContact = mayerContact, subject = "Hello",
                decryptedBody = "Please reply to all.", isDraft = false, fileKey = null)

        val worker = newWorker(emailId = emailId, type = ComposerType.ReplyAll(originalId = emailId,
                currentLabel = Label.defaultItems.inbox, threadPreview = emailPreview))
        val result = worker.work(mockk()) as ComposerResult.LoadInitialData.Success

        result.initialData.subject `shouldEqual` "RE: Hello"
        result.initialData.body `shouldContain` "Please reply to all."
        result.initialData.to `shouldEqual` listOf(danielContact, mayerContact)
    }

    @Test
    fun should_load_an_email_to_forward_correctly() {
        val emailId = insertEmailToLoad(to = listOf(testerContact),
                fromContact = mayerContact, subject = "Hello",
                decryptedBody = "This is something you should forward.", isDraft = false, fileKey = null)

        val worker = newWorker(emailId = emailId, type = ComposerType.Forward(originalId = emailId,
                currentLabel = Label.defaultItems.inbox, threadPreview = emailPreview))
        val result = worker.work(mockk()) as ComposerResult.LoadInitialData.Success

        result.initialData.subject `shouldEqual` "FW: Hello"
        result.initialData.body `shouldContain` "This is something you should forward."
        result.initialData.to `shouldEqual` emptyList()
    }
}