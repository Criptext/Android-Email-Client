package com.criptext.mail.scenes.settings.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.Config
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.workers.CreateCustomLabelWorker
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateCustomLabelWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var settingsLocalDB: SettingsLocalDB
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")
    private lateinit var httpClient: HttpClient

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
        settingsLocalDB = SettingsLocalDB.Default(db)
    }

    @Test
    fun test_should_create_a_custom_label(){
        if (Config.mockCriptextHTTPRequests) {
            mockWebServer.enqueueResponses(listOf(
                    MockedResponse.Ok("")
            ))
        }
        val labelName = "__LABEL__"
        val worker = newWorker(labelName)
        val result = worker.work(mockk()) as SettingsResult.CreateCustomLabel.Success

        result.label.text shouldEqual labelName

        db.labelDao().get(labelName) shouldNotBe null
    }

    private fun newWorker(labelName: String): CreateCustomLabelWorker =
            CreateCustomLabelWorker(
                    labelName = labelName,
                    settingsLocalDB = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = httpClient,
                    publishFn = {})

    @After
    fun teardown() {
        if (Config.mockCriptextHTTPRequests)
            mockWebServer.close()
    }
}