package com.criptext.mail.scenes.settings.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
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
class LogoutWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var settingsLocalDB: SettingsLocalDB
    private lateinit var storage: KeyValueStorage
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
        storage = mockk(relaxed = true)
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        mailboxLocalDB = MailboxLocalDB.Default(db)
        settingsLocalDB = SettingsLocalDB.Default(db)

        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox))
    }

    @Test
    fun should_keep_non_signal_data_in_db_on_logout_and_save_user_id_in_shared_prefs(){

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("")
        ))

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.inbox.text
        ).size shouldBe 2


        val worker = newWorker()

        worker.work(mockk()) as SettingsResult.Logout.Success

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.inbox.text
        ).size shouldBe 2

    }

    private fun newWorker(): LogoutWorker =

            LogoutWorker(
                    db = settingsLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    publishFn = {})

    @After
    fun teardown() {
        mockWebServer.close()
    }

}