package com.criptext.mail.scenes.mailbox.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.scenes.mailbox.workers.MoveEmailThreadWorker
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.junit.After
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
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")
    private lateinit var httpClient: HttpClient
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        mailboxLocalDB = MailboxLocalDB.Default(db)

        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox))
    }

    @Test
    fun test_should_move_two_emails_to_spam_folder(){

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("")
        ))

        mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@criptext.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.spam),
                labelName = Label.defaultItems.spam.text,
                startDate = null,
                limit = 20
        ).size shouldBe 0

        val emailThreads = mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@criptext.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.inbox),
                labelName = Label.defaultItems.inbox.text,
                startDate = null,
                limit = 20
        )

        val worker = newWorker(
                chosenLabel = Label.defaultItems.spam.text,
                currentLabel = Label.defaultItems.inbox,
                selectedThreadIds = emailThreads.map { it.threadId }
        )
        worker.work(mockk()) as MailboxResult.MoveEmailThread.Success

        mailboxLocalDB.getThreadsFromMailboxLabel(
                userEmail = "gabriel@criptext.com",
                rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(Label.defaultItems.spam),
                labelName = Label.defaultItems.spam.text,
                startDate = null,
                limit = 20
        ).size shouldBe 2

    }

    private fun newWorker(chosenLabel: String?,
                          selectedThreadIds: List<String>,
                          currentLabel: Label): MoveEmailThreadWorker =

            MoveEmailThreadWorker(
                    db = mailboxLocalDB,
                    pendingDao = db.pendingEventDao(),
                    currentLabel = currentLabel,
                    chosenLabel = chosenLabel,
                    selectedThreadIds = selectedThreadIds,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    publishFn = {})

    @After
    fun teardown() {
        mockWebServer.close()
    }

}