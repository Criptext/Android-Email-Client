package com.criptext.mail.scenes.settings.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.Config
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.labels.data.LabelsResult
import com.criptext.mail.scenes.settings.labels.workers.CreateCustomLabelWorker
import com.criptext.mail.scenes.settings.labels.workers.DeleteCustomLabelWorker
import com.criptext.mail.utils.ExpectedRequest
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.assertSentRequests
import com.criptext.mail.utils.enqueueResponses
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteCustomLabelWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var settingsLocalDB: SettingsLocalDB
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD)
    private lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        storage = mockk(relaxed = true)
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(activeAccount.id, activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, "", "criptext.com",
                true, true, type = AccountTypes.STANDARD, blockRemoteContent = false,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null))
        settingsLocalDB = SettingsLocalDB.Default(db)
    }

    @Test
    fun test_should_create_and_delete_a_custom_label(){
        if (Config.mockCriptextHTTPRequests) {
            mockWebServer.enqueueResponses(listOf(
                    MockedResponse.Ok(""),
                    MockedResponse.Ok("")
            ))
        }
        val labelName = "__LABEL__"
        val createWorker = createLabelWorker(labelName)
        val createResult = createWorker.work(mockk()) as LabelsResult.CreateCustomLabel.Success

        createResult.label.text shouldEqual labelName

        db.labelDao().get(labelName, activeAccount.id) shouldNotBe null

        val labelUUID = createResult.label.uuid
        val deleteWorker = deleteLabelWorker(labelUUID)
        val deleteResult = deleteWorker.work(mockk()) as LabelsResult.DeleteCustomLabel.Success

        deleteResult.uuid shouldEqual labelUUID

        db.labelDao().getByUUID(labelUUID, activeAccount.id) shouldBe null
    }

    private fun deleteLabelWorker(uuid: String): DeleteCustomLabelWorker =
            DeleteCustomLabelWorker(
                    labelUUID = uuid,
                    settingsLocalDB = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = {},
                    storage = storage)

    private fun createLabelWorker(labelName: String): CreateCustomLabelWorker =
            CreateCustomLabelWorker(
                    labelName = labelName,
                    settingsLocalDB = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = {},
                    storage = storage)

    @After
    fun teardown() {
        if (Config.mockCriptextHTTPRequests)
            mockWebServer.close()
    }
}