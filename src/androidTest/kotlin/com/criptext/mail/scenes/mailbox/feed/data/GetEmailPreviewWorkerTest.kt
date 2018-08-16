package com.criptext.mail.scenes.mailbox.feed.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import io.mockk.mockk
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class GetEmailPreviewWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB

    private val userEmail = "daniel@criptext.com"

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        mailboxLocalDB = MailboxLocalDB.Default(db)
    }

    private fun insertEmailNeededForTests(): Email {
        val fromContact = Contact(1,"mayer@criptext.com", "Mayer Mizrachi")
        val metadata = EmailMetadata.DBColumns(to = userEmail,  cc = "", bcc = "",
                fromContact = fromContact, messageId = "daniel/1/1",
                date = "2018-02-21 14:00:00", threadId = "thread#1",
                subject = "__SUBJECT__", unread = true, metadataKey = 100L, status = DeliveryTypes.NONE,
                unsentDate = "2018-02-21 14:00:00")
        val decryptedBody = "Hello, this is message"
        val labels = listOf(Label.defaultItems.inbox)
        val emailId = EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                decryptedBody = decryptedBody, labels = labels, files = emptyList(), fileKey = null)
        return db.emailDao().findEmailById(emailId)!!
    }

    @Test
    fun test_should_found_two_emails_based_on_sender_email_address(){

        val email = insertEmailNeededForTests()

        val worker = newWorker(email)

        val result = worker.work(mockk()) as FeedResult.GetEmailPreview.Success

        result.emailPreview.subject shouldEqual email.subject
    }

    private fun newWorker(email: Email): GetEmailPreviewWorker =
            GetEmailPreviewWorker(
                    email = email,
                    userEmail = userEmail,
                    mailboxLocalDB = mailboxLocalDB,
                    publishFn = {})

}