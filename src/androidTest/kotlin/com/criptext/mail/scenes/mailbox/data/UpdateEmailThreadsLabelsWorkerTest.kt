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
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.workers.UpdateEmailThreadsLabelsWorker
import com.criptext.mail.utils.*
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UpdateEmailThreadsLabelsWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())

        mailboxLocalDB = MailboxLocalDB.Default(db)

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox))
    }

    private fun newWorker(selectedThreadIds: List<String>, currentLabel: Label, selectedLabels: SelectedLabels):
            UpdateEmailThreadsLabelsWorker =
            UpdateEmailThreadsLabelsWorker(activeAccount = activeAccount,
                    publishFn = {}, httpClient = httpClient, shouldRemoveCurrentLabel = false,
                    selectedThreadIds = selectedThreadIds, currentLabel = currentLabel, db = mailboxLocalDB,
                    selectedLabels = selectedLabels, pendingDao = db.pendingEventDao())

    @Test
    fun when_marked_as_star_should_send_only_star_to_added_labels_and_nothing_on_removed() {
        val expectedThreads = mailboxLocalDB.getThreadsIdsFromLabel(Label.LABEL_INBOX)

        val selectedLabels = SelectedLabels()
        selectedLabels.addMultipleSelected(listOf(LabelWrapper(Label.defaultItems.inbox),
                LabelWrapper(Label.defaultItems.starred)))

        val selectedThread = listOf(expectedThreads[0])


        val updateLabelsWorker = newWorker(selectedLabels = selectedLabels,
                currentLabel = Label.defaultItems.inbox,
                selectedThreadIds = selectedThread)

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("OK")
        ))

        updateLabelsWorker.work(mockk(relaxed = true))
                as MailboxResult.UpdateEmailThreadsLabelsRelations.Success

        mockWebServer.assertSentRequests(listOf(
                ExpectedRequest(
                        expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt),
                        method = "POST", path = "/event/peers",
                        assertBodyFn = {it shouldEqual """{"peerEvents":["{\"cmd\":304,\"params\":{\"threadIds\":[\"${selectedThread[0]}\"],\"labelsRemoved\":[],\"labelsAdded\":[\"Starred\"]}}"]}"""})
        ))

    }

    @After
    fun teardown() {
        mockWebServer.close()
    }
}