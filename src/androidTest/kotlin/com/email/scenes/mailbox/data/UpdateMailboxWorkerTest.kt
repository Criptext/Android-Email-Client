package com.email.scenes.mailbox.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.HttpClient
import com.email.db.DeliveryTypes
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.mocks.MockEmailData
import com.email.mocks.MockJSONData
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.utils.*
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by gabriel on 6/28/18.
 */

@RunWith(AndroidJUnit4::class)
class UpdateMailboxWorkerTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var emailInsertionDao: EmailInsertionDao
    private lateinit var signalClient: SignalClient
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
        emailInsertionDao = db.emailInsertionDao()
        signalClient = SignalClient.Default(SignalStoreCriptext(db))
        mailboxLocalDB = MailboxLocalDB.Default(db)

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
    }

    private fun newWorker(loadedThreadsCount: Int, label: Label): UpdateMailboxWorker =
            UpdateMailboxWorker(signalClient = signalClient, db = mailboxLocalDB,
                    emailDao = db.emailDao(), dao = emailInsertionDao, label = label,
                    activeAccount = activeAccount, loadedThreadsCount = loadedThreadsCount,
                    publishFn = {}, httpClient = httpClient)

    private val hasDeliveryTypeRead: (Email) -> Boolean  = { it.delivered == DeliveryTypes.READ }

    @Test
    fun when_processing_tracking_updates_should_mark_emails_as_read_in_the_db() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2TrackingUpdateEvents), /* /event */
            MockedResponse.Ok("OK") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = listOf(
                MockEmailData.createNewEmail(1),
                MockEmailData.createNewEmail(2))
        db.emailDao().insertAll(localEmails)
        Log.d("DeliveryStatus", "insert local emails $localEmails")

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success

        // assert that emails got updated correctly in DB
        val updatedEmails = db.emailDao().getAll()
        updatedEmails.size `shouldBe` 2
        Log.d("DeliveryStatus", "updatedEmails = ${updatedEmails.map { it.delivered }}")
        updatedEmails.all(hasDeliveryTypeRead).shouldBeTrue()
    }

    @Test
    fun when_processing_tracking_updates_should_fetch_and_acknowledge_events_correctly() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2TrackingUpdateEvents), /* /event */
            MockedResponse.Ok("OK") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = listOf(
                MockEmailData.createNewEmail(1),
                MockEmailData.createNewEmail(2))
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success

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
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success

        // assert that emails got inserted correctly in DB
        val newLocalEmails = db.emailDao().getAll()
        newLocalEmails.size `shouldBe` 5
        val getEmailBody: (Email) -> String = { it.content }

        // assert that the new emails got in
        val latestEmails = newLocalEmails.subList(3, 5)
        latestEmails.map(getEmailBody).shouldEqual(
                listOf("Unable to decrypt message.", // emails cant be decrypted because they are fake
                        "Unable to decrypt message.")
        )

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
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success

        // assert that requests got sent correctly
        val expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt)
        mockWebServer.assertSentRequests(
                listOf(
                        ExpectedRequest(method = "GET", path = "/event",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                                ),
                        ExpectedRequest(method = "GET", path = "/email/body/%3C15221916.12518@jigl.com%3E",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "GET", path = "/email/body/%3C15221916.12519@jigl.com%3E",
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
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /event */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Timeout(), /* /email/body (2nd email) */
            MockedResponse.Ok("OK") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success

        // assert that requests got sent correctly
        val expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt)
        mockWebServer.assertSentRequests(
                listOf(
                        ExpectedRequest(method = "GET", path = "/event",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                                ),
                        ExpectedRequest(method = "GET", path = "/email/body/%3C15221916.12518@jigl.com%3E",
                                expectedAuthScheme = expectedAuthScheme, assertBodyFn = null
                        ),
                        ExpectedRequest(method = "GET", path = "/email/body/%3C15221916.12519@jigl.com%3E",
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
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /event */
            MockedResponse.Ok("__ENCRYPTED_BODY_1"), /* /email/body (1st email) */
            MockedResponse.Ok("__ENCRYPTED_BODY_2"), /* /email/body (2nd email) */
            MockedResponse.Timeout(), /* /event/ack */
            // second run
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /event */
            MockedResponse.Ok("Ok") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3)
        db.emailDao().insertAll(localEmails)

        val worker = newWorker(2, Label.defaultItems.inbox)

        // run worker 1st time
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success
        val mailCountAfter1stRun = db.emailDao().getAll().size

        // run worker 2nd time
        worker.work(mockk()) as MailboxResult.UpdateMailbox.Success
        val mailCountAfter2ndRun = db.emailDao().getAll().size

        // no new mails should be added on second run
        mailCountAfter2ndRun shouldEqual mailCountAfter1stRun
    }
}
