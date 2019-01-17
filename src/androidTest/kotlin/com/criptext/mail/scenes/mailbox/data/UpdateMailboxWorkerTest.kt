package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.util.Log
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.mocks.MockJSONData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.*
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.workers.UpdateMailboxWorker
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by gabriel on 6/28/18.
 */

@RunWith(AndroidJUnit4::class)
class UpdateMailboxWorkerTest {

    private lateinit var emailInsertionDao: EmailInsertionDao
    private lateinit var signalClient: SignalClient
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private lateinit var storage: KeyValueStorage
    protected lateinit var eventDB: EventLocalDB
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "")
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    private lateinit var db: TestDatabase

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        emailInsertionDao = db.emailInsertionDao()
        signalClient = SignalClient.Default(SignalStoreCriptext(db))
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        eventDB = EventLocalDB(db, mActivityRule.activity.filesDir)

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        storage = mockk(relaxed = true)
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
    }


    private fun newWorker(loadedThreadsCount: Int, label: Label): UpdateMailboxWorker =
            UpdateMailboxWorker(signalClient = signalClient, label = label,
                    activeAccount = activeAccount, loadedThreadsCount = loadedThreadsCount,
                    publishFn = {}, httpClient = httpClient, dbEvents = eventDB, storage = storage,
                    pendingEventDao = db.pendingEventDao(), accountDao = db.accountDao())

    private val hasDeliveryTypeRead: (Email) -> Boolean  = { it.delivered == DeliveryTypes.READ }

    @Test
    fun when_processing_tracking_updates_should_mark_emails_as_read_in_the_db_and_create_feeds() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2TrackingUpdateEvents), /* /data */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = listOf(
                MockEmailData.createNewEmail(1),
                MockEmailData.createNewEmail(2))
        db.emailDao().insertAll(localEmails)
        db.contactDao().insertIgnoringConflicts(Contact(
                id = 0,
                email = "mayer@criptext.com",
                name = "Mayer"
        ))
        Log.d("DeliveryStatus", "insert local emails $localEmails")

        var totalFeeds = db.feedDao().getAllFeedItems().size
        totalFeeds `shouldBe` 0

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success

        // assert that emails got updated correctly in DB
        val updatedEmails = db.emailDao().getAll()
        updatedEmails.size `shouldBe` 2
        Log.d("DeliveryStatus", "updatedEmails = ${updatedEmails.map { it.delivered }}")
        updatedEmails.all(hasDeliveryTypeRead).shouldBeTrue()

        totalFeeds = db.feedDao().getAllFeedItems().size
        totalFeeds `shouldBe` 2
    }

    @Test
    fun when_processing_tracking_updates_should_fetch_and_acknowledge_events_correctly() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2TrackingUpdateEvents), /* /event */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = listOf(
                MockEmailData.createNewEmail(1),
                MockEmailData.createNewEmail(2))
        db.emailDao().insertAll(localEmails)
        db.contactDao().insertIgnoringConflicts(Contact(1, "mayer@criptext.com", "Mayer"))

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success

        // assert that requests got sent correctly
        val expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt)
        mockWebServer.assertSentRequests(
                listOf(
                        ExpectedRequest(method = "GET", path = "/event",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                                ),
                        ExpectedRequest(method = "POST", path = "/event/ack",
                                expectedAuthScheme = expectedAuthScheme,
                                assertBodyFn = { it `shouldEqual` """{"ids":[1,2]}""" })
                )
        )
    }

    @Test
    fun when_processing_new_email_events_should_insert_emails_correctly() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Ok("__ENCRYPTED_BODY_2"), /* /email/body (2st email) */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success

        // assert that emails got inserted correctly in DB
        val newLocalEmails = db.emailDao().getAll()
        newLocalEmails.size `shouldBe` 5
        // assert that the new emails got in
        val latestEmails = newLocalEmails.subList(3, 5)
        latestEmails.forEach { email ->
            email.content.shouldBeEqualTo("<html>\n" +
                    " <head></head>\n" +
                    " <body>\n" +
                    "  Unable to decrypt message.\n" +
                    " </body>\n" +
                    "</html>")
        }
    }

    @Test
    fun when_processing_new_email_events_should_fetch_and_acknowledge_events_correctly() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /event */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Ok("__ENCRYPTED_BODY_2"), /* /email/body (2st email) */
            MockedResponse.Ok("OK") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success

        // assert that requests got sent correctly
        val expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt)
        mockWebServer.assertSentRequests(
                listOf(
                        ExpectedRequest(method = "GET", path = "/event",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                                ),
                        ExpectedRequest(method = "GET", path = "/email/body/81",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "GET", path = "/email/body/82",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "POST", path = "/event/ack",
                                expectedAuthScheme = expectedAuthScheme,
                                assertBodyFn = { it `shouldEqual` """{"ids":[4,5]}""" })
                )
        )
    }

    @Test
    fun when_processing_new_email_events_if_get_body_fails_should_not_acknowledge_that_event() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Timeout(), /* /email/body (2nd email) */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success

        // assert that requests got sent correctly
        val expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt)
        mockWebServer.assertSentRequests(
                listOf(
                        ExpectedRequest(method = "GET", path = "/event",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                                ),
                        ExpectedRequest(method = "GET", path = "/email/body/81",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "GET", path = "/email/body/82",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "POST", path = "/event/ack",
                                expectedAuthScheme = expectedAuthScheme,
                                assertBodyFn = { it `shouldEqual` """{"ids":[4]}""" })
                )
        )
    }

    @Test
    fun when_processing_new_email_events_if_acknowledge_fails_should_retry_acknowledge_on_2nd_run() {
        mockWebServer.enqueueResponses(listOf(
            // first run
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Ok("__ENCRYPTED_BODY_2"), /* /email/body (2nd email) */
            MockedResponse.Timeout(), /* /data/ack */
            // second run
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok("Ok") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        val worker = newWorker(2, Label.defaultItems.inbox)

        // run worker 1st time
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success
        val mailCountAfter1stRun = db.emailDao().getAll().size

        // run worker 2nd time
        worker.work(mockk()) as GeneralResult.UpdateMailbox.Success
        val mailCountAfter2ndRun = db.emailDao().getAll().size

        // no new mails should be added on second run
        mailCountAfter2ndRun shouldEqual mailCountAfter1stRun
    }
}
