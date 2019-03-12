package com.criptext.mail.scenes.mailbox.feed.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.utils.EmailUtils
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

    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(activeAccount.id, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, "", "criptext.com",
                true, true))
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
    }

    private fun insertEmailNeededForTests(): Email {
        val fromContact = Contact(1,"mayer@criptext.com", "Mayer Mizrachi",
                isTrusted = false, score = 0)
        val metadata = EmailMetadata.DBColumns(to = listOf(activeAccount.userEmail),
                cc = emptyList(), bcc = emptyList(),
                fromContact = fromContact, messageId = "daniel/1/1",
                date = "2018-02-21 14:00:00", threadId = "thread#1",
                subject = "__SUBJECT__", unread = true, metadataKey = 100L, status = DeliveryTypes.NONE,
                unsentDate = "2018-02-21 14:00:00", secure = true, trashDate = "2018-02-21 14:00:00",
                boundary = null, replyTo = null)
        val decryptedBody = "Hello, this is message"
        val labels = listOf(Label.defaultItems.inbox)
        EmailUtils.saveEmailInFileSystem(
                filesDir = mActivityRule.activity.filesDir,
                recipientId = activeAccount.recipientId,
                metadataKey = metadata.metadataKey,
                content = decryptedBody,
                headers = null)
        val emailId = EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                preview = decryptedBody, labels = labels, files = emptyList(), fileKey = null, accountId = activeAccount.id)
        return db.emailDao().findEmailById(emailId, activeAccount.id)!!
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
                    userEmail = activeAccount.userEmail,
                    mailboxLocalDB = mailboxLocalDB,
                    publishFn = {})

}