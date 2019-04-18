package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.scenes.mailbox.workers.EmptyTrashWorker
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
class EmptyTrashWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)
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
        db.accountDao().insert(Account(id = 1, recipientId = "tester", deviceId = 1,
                name = "Tester", registrationId = 1,
                identityKeyPairB64 = "_IDENTITY_", jwt = "__JWTOKEN__",
                signature = "", refreshToken = "__REFRESH__", isActive = true, domain = "criptext.com", isLoggedIn = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null))
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        storage = mockk(relaxed = true)
        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.trash),
                mActivityRule.activity.filesDir, activeAccount.recipientId, accountId = activeAccount.id)
    }

    @Test
    fun should_delete_permanently_all_emails_in_trash(){

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("")
        ))

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.trash.text,
                accountId = activeAccount.id
        ).size shouldBe 2


        val worker = newWorker()

        worker.work(mockk()) as MailboxResult.EmptyTrash.Success

        mailboxLocalDB.getThreadsIdsFromLabel(
                labelName = Label.defaultItems.trash.text,
                accountId = activeAccount.id
        ).size shouldBe 0

    }

    private fun newWorker(): EmptyTrashWorker =

            EmptyTrashWorker(
                    db = mailboxLocalDB,
                    httpClient = httpClient,
                    activeAccount = activeAccount,
                    pendingDao = db.pendingEventDao(),
                    publishFn = {},
                    accountDao = db.accountDao(),
                    storage = storage)

    @After
    fun teardown() {
        mockWebServer.close()
    }

}