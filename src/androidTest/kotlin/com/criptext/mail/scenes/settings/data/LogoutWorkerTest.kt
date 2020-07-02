package com.criptext.mail.scenes.settings.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.*
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.workers.LogoutWorker
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
    private lateinit var eventLocalDB: EventLocalDB
    private lateinit var storage: KeyValueStorage
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD, blockRemoteContent = true,
            defaultAddress = null)
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
        db.accountDao().insert(Account(id = 1, recipientId = "tester", deviceId = 1,
                name = "Tester", registrationId = 1,
                identityKeyPairB64 = "_IDENTITY_", jwt = "__JWTOKEN__",
                signature = "", refreshToken = "__REFRESH__", isActive = true, domain = "criptext.com", isLoggedIn = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null,
                type = AccountTypes.STANDARD, blockRemoteContent = true, defaultAddress = null))
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        eventLocalDB = EventLocalDB(db, mActivityRule.activity.filesDir, mActivityRule.activity.cacheDir)

        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox),
                mActivityRule.activity.filesDir, activeAccount.recipientId, accountId = activeAccount.id,
                domain = activeAccount.domain)
    }

    @Test
    fun should_keep_non_signal_data_in_db_on_logout_and_save_user_id_in_shared_prefs(){

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("")
        ))

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.inbox.text,
                accountId = activeAccount.id
        ).size shouldBe 2


        val worker = newWorker()

        worker.work(mockk()) as GeneralResult.Logout.Success

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.inbox.text,
                accountId = activeAccount.id
        ).size shouldBe 2

    }

    private fun newWorker(): LogoutWorker =

            LogoutWorker(
                    db = eventLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    storage = storage,
                    accountDao = db.accountDao(),
                    shouldDeleteAllData = false,
                    letAPIKnow = true,
                    publishFn = {})

    @After
    fun teardown() {
        mockWebServer.close()
    }

}