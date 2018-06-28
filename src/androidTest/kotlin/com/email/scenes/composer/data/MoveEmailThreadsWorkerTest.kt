package com.email.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.models.EmailMetadata
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.models.*
import com.email.scenes.mailbox.data.EmailInsertionSetup
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxResult
import com.email.scenes.mailbox.data.MoveEmailThreadWorker
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoveEmailThreadsWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        mailboxLocalDB = MailboxLocalDB.Default(db)

        val fromContact = Contact(1,"mayer@jigl.com", "Mayer Mizrachi")
        (1..2).forEach {
            val seconds = if (it < 10) "0$it" else it.toString()
            val metadata = EmailMetadata.DBColumns(to = "gabriel@jigl.com",  cc = "", bcc = "",
                    fromContact = fromContact, messageId = "gabriel/1/$it",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                    subject = "Test #$it", unread = true, metadataKey = it + 100L)
            val decryptedBody = "Hello, this is message #$it"
            val labels = listOf(Label.defaultItems.inbox)
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    decryptedBody = decryptedBody, labels = labels, files = emptyList())
        }
    }

    @Test
    fun test_should_move_two_emails_to_spam_folder(){

        mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@jigl.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.spam),
                labelTextTypes = Label.defaultItems.spam.text,
                oldestEmailThread = null,
                limit = 20
        ).size shouldBe 0

        val emailThreads = mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@jigl.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox),
                labelTextTypes = Label.defaultItems.inbox.text,
                oldestEmailThread = null,
                limit = 20
        )

        val worker = newWorker(
                chosenLabel = Label.defaultItems.spam.text,
                currentLabel = Label.defaultItems.inbox,
                selectedEmailThreads = emailThreads
        )
        worker.work(mockk()) as MailboxResult.MoveEmailThread.Success

        mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@jigl.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.spam),
                labelTextTypes = Label.defaultItems.spam.text,
                oldestEmailThread = null,
                limit = 20
        ).size shouldBe 2

    }

    private fun newWorker(chosenLabel: MailFolders?,
                          selectedEmailThreads: List<EmailThread>,
                          currentLabel: Label): MoveEmailThreadWorker =

            MoveEmailThreadWorker(
                    db = mailboxLocalDB,
                    currentLabel = currentLabel,
                    chosenLabel = chosenLabel,
                    selectedEmailThreads = selectedEmailThreads,
                    publishFn = {})

}