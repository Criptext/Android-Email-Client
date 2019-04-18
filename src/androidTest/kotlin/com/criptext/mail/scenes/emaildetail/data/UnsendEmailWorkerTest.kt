package com.criptext.mail.scenes.emaildetail.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.Config
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.androidtest.TestSharedPrefs
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.*
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.workers.SaveEmailWorker
import com.criptext.mail.scenes.emaildetail.workers.UnsendFullEmailWorker
import com.criptext.mail.scenes.mailbox.workers.SendMailWorker
import com.criptext.mail.signal.*
import com.criptext.mail.utils.*
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.amshove.kluent.shouldNotBeEqualTo
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.TemporaryFolder


@RunWith(AndroidJUnit4::class)
class UnsendEmailWorkerTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var emailDetailLocalDB: EmailDetailLocalDB
    private lateinit var signalClient: SignalClient
    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)
    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)

        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(1, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 1, "", "criptext.com",
                true, true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null))
        emailDetailLocalDB = EmailDetailLocalDB.Default(db, mActivityRule.activity.filesDir)
        storage = mockk(relaxed = true)
        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox),
                mActivityRule.activity.filesDir, activeAccount.recipientId, listOf("gabriel@criptext.com", "mayer@gmail.com"),
                activeAccount.id)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
    }

    private fun newWorker(emailId: Long, position: Int): UnsendFullEmailWorker =
            UnsendFullEmailWorker(db = emailDetailLocalDB, emailDao = db.emailDao(),
                    emailContactDao = db.emailContactDao(), emailId = emailId, position = position,
                    accountDao = db.accountDao(), storage = storage, httpClient = httpClient,
                    activeAccount = activeAccount, publishFn = {})

    @Test
    fun should_delete_email_content_on_unsend() {

        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("Ok")
        ))

        val emails = db.emailDao().getAll(activeAccount.id)
        val emailToUnsend = emails[0]

        val emailContentBeforeUnsend = EmailUtils.getEmailContentFromFileSystem(
                dbContent = emailToUnsend.content,
                filesDir = mActivityRule.activity.filesDir,
                metadataKey = emailToUnsend.metadataKey,
                recipientId = activeAccount.recipientId
        )

        emailContentBeforeUnsend.first shouldNotBeEqualTo ""


        val worker = newWorker(emailToUnsend.id, 0)
        worker.work(mockk(relaxed = true))
                as EmailDetailResult.UnsendFullEmailFromEmailId.Success

        //The request should only be done with Criptext recipients, so we test the worker if it correctly filters the to addresses.
        if(Config.mockCriptextHTTPRequests) {
            mockWebServer.assertSentRequests(listOf(
                    ExpectedRequest(
                            expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt),
                            method = "POST", path = "/email/unsend",
                            assertBodyFn = { it `shouldEqual` """{"metadataKey":101,"recipients":["gabriel@criptext.com"]}""" })
            ))
        }

        val unsentEmailFromDB = db.emailDao().getEmailById(emailToUnsend.id, activeAccount.id)

        unsentEmailFromDB shouldNotBe null

        val emailContentAfterUnsend = EmailUtils.getEmailContentFromFileSystem(
                dbContent = unsentEmailFromDB!!.content,
                filesDir = mActivityRule.activity.filesDir,
                metadataKey = unsentEmailFromDB.metadataKey,
                recipientId = activeAccount.recipientId
        )

        emailContentAfterUnsend.first shouldEqual ""

    }

}