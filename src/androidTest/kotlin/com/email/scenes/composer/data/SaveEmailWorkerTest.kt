package com.email.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.bgworker.ProgressReporter
import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.Label
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
    private lateinit var emailInsertionDao: EmailInsertionDao
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")

    private val progressReporter: ProgressReporter<ComposerResult.SaveEmail> = mockk()

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        emailInsertionDao = db.emailInsertionDao()
    }

    private fun newWorker(emailId: Long?, threadId: String?, onlySave: Boolean,
                          inputData: ComposerInputData): SaveEmailWorker =
            SaveEmailWorker(emailId = emailId, threadId = threadId, composerInputData = inputData,
                    onlySave = onlySave, account = activeAccount, dao = emailInsertionDao,
                    publishFn = {}, attachments = emptyList())

    @Test
    fun test_should_save_new_composed_email_along_with_new_contacts() {
        val toRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@jigl.com"),
                Contact(id = 0, name = "", email = "daniel@jigl.com")
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@jigl.com")
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Email", body = "Hello, this is a test email")

        val worker = newWorker(emailId = null, threadId = null, onlySave = false,
                inputData = inputData)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedEmails = db.emailDao().getAll()
        val insertedContacts = db.contactDao().getAll()
        val insertedContactsAddresses = insertedContacts.map { contact -> contact.email }

        insertedEmails.single().content `shouldEqual` "Hello, this is a test email"
        insertedContactsAddresses `shouldEqual` listOf(
                "tester@jigl.com", "mayer@jigl.com", "daniel@jigl.com", "gianni@jigl.com")
    }

    @Test
    fun test_should_save_a_restored_draft_deleting_the_previous_row() {
        // insert previously generated draft to DB
        val previousDraft = Email(id = 0, messageId = "__MESSAGE_ID__", unread = false,
                content = "This was my original draft", preview = "__PREVIEW__", subject = "draft",
                delivered = DeliveryTypes.NONE, date = Date(), secure = false,
                threadId = "__MESSAGE_ID__", metadataKey = 1246275862L, isMuted = false)
        val draftId = db.emailDao().insert(previousDraft)


        // Now create the finished draft
        val toRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@jigl.com"),
                Contact(id = 0, name = "", email = "daniel@jigl.com")
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@jigl.com")
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Finished Draft", body = "Hello, I have finished my draft")

        val worker = newWorker(emailId = draftId, threadId = "__MESSAGE_ID__", onlySave = false,
                inputData = inputData)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedEmails = db.emailDao().getAll()

        insertedEmails.single().content `shouldEqual` "Hello, I have finished my draft"
    }

    @Test
    fun should_not_touch_already_existing_contacts() {
        // Insert the "already existing" contacts
        val existingRecipients = listOf(
                Contact(id = 0, name = "", email = "mayer@jigl.com"),
                Contact(id = 0, name = "", email = "daniel@jigl.com")
        )
        db.contactDao().insertAll(existingRecipients)

        // Now create the email and insert it with the existing contacts
        val toRecipients = listOf(
                Contact(id = 1, name = "", email = "mayer@jigl.com"),
                Contact(id = 2, name = "", email = "daniel@jigl.com")
        )
        val ccRecipients = listOf(
                Contact(id = 0, name = "", email = "gianni@jigl.com")
        )
        val inputData = ComposerInputData(to = toRecipients, cc = ccRecipients, bcc = emptyList(),
                subject = "Test Email", body = "Hello, this is a test email")

        val worker = newWorker(emailId = null, threadId = null, onlySave = false,
                inputData = inputData)

        worker.work(progressReporter) as ComposerResult.SaveEmail.Success

        // assert that data actually got stored to DB
        val insertedContacts = db.contactDao().getAll()
        val insertedContactsAddresses = insertedContacts.map { contact -> contact.email }

        insertedContactsAddresses `shouldEqual` listOf(
                "mayer@jigl.com", "daniel@jigl.com", "tester@jigl.com", "gianni@jigl.com")
    }
}