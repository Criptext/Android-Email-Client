package com.criptext.mail.scenes.composer.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.Config
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.workers.UploadAttachmentWorker
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.AndroidFs
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class UploadAttachmentWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)
    private val fileServiceAuthToken =
            Encoding.byteArrayToString(
                    "qynhtyzjrshazxqarkpy:lofjksedbxuucdjjpnby".toByteArray(
                            Charset.forName("UTF-8")
                    )
            )

    private lateinit var httpClient: HttpClient

    private fun getFilServiceBaseUrl(): String {
        val realBaseUrl = "http://services.criptext.com"
        return if (Config.mockCriptextHTTPRequests) {
            mockWebServer = MockWebServer()
            mockWebServer.start()
            mockWebServer.url("/mock").toString()
        } else
            realBaseUrl
    }

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        storage = mockk(relaxed = true)
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = getFilServiceBaseUrl(), connectionTimeout = 7000L,
                readTimeout = 7000L)
    }

    @After
    fun teardown() {
        if (Config.mockCriptextHTTPRequests)
            mockWebServer.close()
    }

    private fun newWorker(filepath: String): UploadAttachmentWorker =
            UploadAttachmentWorker(filepath = filepath, activeAccount = activeAccount,
                    httpClient = httpClient, publishFn = {}, fileKey = null,
                    accountDao =  db.accountDao(), filesSize = 0L, storage = storage)

    @Test
    fun should_upload_file_without_errors() {

        val fileToUpload = AndroidFs.getFileFromImageCache(mActivityRule.activity,
                testBinaryFileName)

        if (Config.mockCriptextHTTPRequests) {
            mockWebServer.enqueueResponses(listOf(
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}""")
            ))
        }

        try {
            FileDownloader.download(testBinaryFileURL, fileToUpload)

            val worker = newWorker(fileToUpload.absolutePath)

            worker.work(mockk(relaxed = true)) as ComposerResult.UploadFile.Success
        } finally {
            fileToUpload.delete()
        }

        if(Config.mockCriptextHTTPRequests) {
            mockWebServer.assertSentRequests(listOf(
                    ExpectedRequest(
                        expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt),
                        method = "POST", path = "/file/upload", assertBodyFn = null),
                    ExpectedRequest(
                            expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt),
                            method = "POST", path = "/file/chunk", assertBodyFn = null),
                    ExpectedRequest(
                            expectedAuthScheme = ExpectedAuthScheme.Jwt(activeAccount.jwt),
                            method = "POST", path = "/file/chunk", assertBodyFn = null)
            ))
        }

    }

    companion object {
        private val testBinaryFileURL = "https://cdn.criptext.com/Email/images/emailhome/icon-dwm-mobile.png"
        private val testBinaryFileName = "my_image.png"
    }


}