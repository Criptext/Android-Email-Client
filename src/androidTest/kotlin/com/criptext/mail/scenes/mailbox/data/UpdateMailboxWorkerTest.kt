package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.util.Log
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.dao.EmailInsertionDao
import com.criptext.mail.db.models.*
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.mocks.MockJSONData
import com.criptext.mail.utils.*
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.workers.ActiveAccountUpdateMailboxWorker
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.*
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.criptext.mail.db.*
import com.criptext.mail.signal.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.*


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
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD, blockRemoteContent = true,
            defaultAddress = null)
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    private lateinit var db: TestDatabase

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(activeAccount.id, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, "", "criptext.com",
                true, true, type = AccountTypes.STANDARD, blockRemoteContent = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true,
                lastTimeBackup = null, defaultAddress = null))
        emailInsertionDao = db.emailInsertionDao()
        //signalClient = SignalClient.Default(SignalStoreCriptext(db))
        signalClient = mockk(relaxed = true)
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        eventDB = EventLocalDB(db, mActivityRule.activity.filesDir, mActivityRule.activity.cacheDir)

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        storage = mockk(relaxed = true)
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
    }


    private fun newWorker(loadedThreadsCount: Int, label: Label): ActiveAccountUpdateMailboxWorker =
            ActiveAccountUpdateMailboxWorker(label = label,
                    publishFn = {}, httpClient = httpClient, dbEvents = eventDB, storage = storage,
                    pendingEventDao = db.pendingEventDao(), accountDao = db.accountDao(), signalClient = signalClient,
                    db = db, account = activeAccount)

    private val hasDeliveryTypeRead: (Email) -> Boolean  = { it.delivered == DeliveryTypes.READ }

    @Test
    fun when_processing_tracking_updates_should_mark_emails_as_read_in_the_db_and_create_feeds() {
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2TrackingUpdateEvents), /* /data */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = listOf(
                MockEmailData.createNewEmail(1, activeAccount.id),
                MockEmailData.createNewEmail(2, activeAccount.id))
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)
        db.contactDao().insertIgnoringConflicts(Contact(
                id = 1,
                email = "mayer@criptext.com",
                name = "Mayer",
                score = 0,
                isTrusted = false
        ))
        db.emailInsertionDao().insertAccountContact(listOf(AccountContact(0, 1, 1)))
        Log.d("DeliveryStatus", "insert local emails $localEmails")

        var totalFeeds = db.feedDao().getAllFeedItems(activeAccount.id).size
        totalFeeds `shouldBe` 0

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success

        // assert that emails got updated correctly in DB
        val updatedEmails = db.emailDao().getAll(activeAccount.id)
        updatedEmails.size `shouldBe` 2
        Log.d("DeliveryStatus", "updatedEmails = ${updatedEmails.map { it.delivered }}")
        updatedEmails.all(hasDeliveryTypeRead).shouldBeTrue()

        totalFeeds = db.feedDao().getAllFeedItems(activeAccount.id).size
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
                MockEmailData.createNewEmail(1, activeAccount.id),
                MockEmailData.createNewEmail(2, activeAccount.id))
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)
        db.contactDao().insertIgnoringConflicts(Contact(1, "mayer@criptext.com", "Mayer",
                false, 0))
        db.emailInsertionDao().insertAccountContact(listOf(AccountContact(0, 1, 1)))

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success

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
        val mockedJsonForGetBody1 = JSONObject()
        mockedJsonForGetBody1.put("body", "__ENCRYPTED_BODY_1")
        mockedJsonForGetBody1.put("headers", "")
        val mockedJsonForGetBody2 = JSONObject()
        mockedJsonForGetBody2.put("body", "__ENCRYPTED_BODY_2")
        mockedJsonForGetBody2.put("headers", "")
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok(mockedJsonForGetBody1.toString()), /* /email/body (1st email) */
            MockedResponse.Ok(mockedJsonForGetBody2.toString()), /* /email/body (2st email) */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3, activeAccount.id)
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success

        // assert that emails got inserted correctly in DB and in the File System
        val newLocalEmails = db.emailDao().getAll(activeAccount.id)
        newLocalEmails.forEach { it.content = EmailUtils.getEmailContentFromFileSystem(
                mActivityRule.activity.filesDir, it.metadataKey, it.content, activeAccount.recipientId
                , activeAccount.domain).first }
        newLocalEmails.size `shouldBe` 5
        // assert that the new emails got in
        val latestEmails = newLocalEmails.subList(3, 5)
        latestEmails.forEach { email ->
            email.content.shouldBeEqualTo("<html>\n" +
                    " <head></head>\n" +
                    " <body></body>\n" +
                    "</html>")
            //Email preview should not contain html tags and should be on the character limit of 300.
            email.preview.shouldBeEqualTo("")
            email.preview.length.shouldBeLessOrEqualTo(300)
        }
    }

    @Test
    fun when_processing_new_email_events_should_fetch_and_acknowledge_events_correctly() {
        val mockedJsonForGetBody1 = JSONObject()
        mockedJsonForGetBody1.put("body", "__ENCRYPTED_BODY_1")
        mockedJsonForGetBody1.put("headers", "")
        val mockedJsonForGetBody2 = JSONObject()
        mockedJsonForGetBody2.put("body", "__ENCRYPTED_BODY_2")
        mockedJsonForGetBody2.put("headers", "")
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /event */
            MockedResponse.Ok(mockedJsonForGetBody1.toString()), /* /email/body (1st email) */
            MockedResponse.Ok(mockedJsonForGetBody2.toString()), /* /email/body (2st email) */
            MockedResponse.Ok("OK") /* /event/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3, activeAccount.id)
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success

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
        val mockedJsonForGetBody = JSONObject()
        mockedJsonForGetBody.put("body", "__ENCRYPTED_BODY_1")
        mockedJsonForGetBody.put("headers", "")
        mockWebServer.enqueueResponses(listOf(
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok(mockedJsonForGetBody.toString()), /* /email/body (1st email) */
            MockedResponse.Timeout(), /* /email/body (2nd email) */
            MockedResponse.Ok("OK") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3, activeAccount.id)
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)

        // run worker
        val worker = newWorker(2, Label.defaultItems.inbox)
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success

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
        val mockedJsonForGetBody1 = JSONObject()
        mockedJsonForGetBody1.put("body", "__ENCRYPTED_BODY_1")
        mockedJsonForGetBody1.put("headers", "")
        val mockedJsonForGetBody2 = JSONObject()
        mockedJsonForGetBody2.put("body", "__ENCRYPTED_BODY_2")
        mockedJsonForGetBody2.put("headers", "")
        mockWebServer.enqueueResponses(listOf(
            // first run
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok(mockedJsonForGetBody1.toString()), /* /email/body (1st email) */
            MockedResponse.Ok(mockedJsonForGetBody2.toString()), /* /email/body (2nd email) */
            MockedResponse.Timeout(), /* /data/ack */
            // second run
            MockedResponse.Ok(MockJSONData.sample2NewEmailEvents), /* /data */
            MockedResponse.Ok("Ok") /* /data/ack */
        ))

        // store local emails in db
        val localEmails = MockEmailData.createNewEmails(3, activeAccount.id)
        localEmails.forEach {
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = it.metadataKey,
                    content = it.content,
                    headers = null,
                    domain = activeAccount.domain)
            it.content = ""
        }
        db.emailDao().insertAll(localEmails)

        val worker = newWorker(2, Label.defaultItems.inbox)

        // run worker 1st time
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success
        val mailCountAfter1stRun = db.emailDao().getAll(activeAccount.id).size

        // run worker 2nd time
        worker.work(mockk()) as GeneralResult.ActiveAccountUpdateMailbox.Success
        val mailCountAfter2ndRun = db.emailDao().getAll(activeAccount.id).size

        // no new mails should be added on second run
        mailCountAfter2ndRun shouldEqual mailCountAfter1stRun
    }
}
