package com.criptext.mail.scenes.emaildetail.data

import android.Manifest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.Config
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.workers.UploadAttachmentWorker
import com.criptext.mail.scenes.emaildetail.workers.DownloadAttachmentWorker
import com.criptext.mail.utils.FileDownloader
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import com.criptext.mail.utils.file.AndroidFs
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldEqualTo
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class DownloadAttachmentWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "")

    private var filetoken = ""
    private val reporter: ProgressReporter<ComposerResult.UploadFile> =
            if(Config.mockCriptextHTTPRequests) mockk(relaxed = true)
            else object: ProgressReporter<ComposerResult.UploadFile> {
                override fun report(progressPercentage: ComposerResult.UploadFile) {
                    when (progressPercentage) {
                        is ComposerResult.UploadFile.Register -> {
                            filetoken = progressPercentage.filetoken
                        }
                    }
                }
            }

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
                    httpClient = httpClient, publishFn = {}, fileKey = null, accountDao = db.accountDao(),
                    storage = storage, filesSize = 0L)

    private fun newDownloadWorker(filetoken: String): DownloadAttachmentWorker =
            DownloadAttachmentWorker(fileToken = filetoken, emailId = 0,
                    downloadPath = mActivityRule.activity.cacheDir.absolutePath,
                    httpClient = httpClient, activeAccount = activeAccount,
                    publishFn = {}, fileKey = null, fileName = "", fileSize = 0L,
                    accountDao = db.accountDao(), storage = storage)

    private fun sendPermanentRequest(filetoken: String){
        val filejson = JSONObject()
        filejson.put("token", filetoken)
        val json = JSONObject()
        val files = JSONArray()
        files.put(filejson)
        json.put("files", files)
        httpClient.post("/file/save", activeAccount.jwt, json)
    }

    @Test
    fun should_download_file_without_errors() {

        val fileToUpload = AndroidFs.getFileFromImageCache(mActivityRule.activity,
                testBinaryFileName)

        /*
        * If server responses are mocked, I can respond with a file token everytime but the fourth
        * request which is a request to obtain the file metadata
        */
        if (Config.mockCriptextHTTPRequests) {
            mockWebServer.enqueueResponses(listOf(
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"file": {"token": "dsfdsfsda", "name": "test.pdf", "chunk_size": 512000, "chunks": 2}}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}"""),
                    MockedResponse.Ok("""{"filetoken":"__FILETOKEN__"}""")
            ))
        }

        try {
            FileDownloader.download(testBinaryFileURL, fileToUpload)

            val worker = newWorker(fileToUpload.absolutePath)
            worker.work(reporter)
                    as ComposerResult.UploadFile.Success

            /*
            * If server responses are not mocked, I need to finish the upload process of the
            * file server and wait around 10 seconds to download the file
            * File server needs time to process the uploaded file before it can be downloaded
            */
            if(!Config.mockCriptextHTTPRequests) {
                sendPermanentRequest(filetoken)
                Thread.sleep(10000)
            }

            val downloadWorker = newDownloadWorker(filetoken)

            val result = downloadWorker.work(mockk(relaxed = true)) as EmailDetailResult.DownloadFile.Success

            /*
            * If server responses are not mocked, I can check the downloaded file size against the
            * uploaded file size
            */
            if (!Config.mockCriptextHTTPRequests) {
                val downloadedFile = File(result.filepath)
                downloadedFile.length() shouldEqualTo fileToUpload.length()
                downloadedFile.delete()
            }
        } finally {
            fileToUpload.delete()
        }

    }

    companion object {
        private val testBinaryFileURL = "https://cdn.criptext.com/Email/images/emailhome/icon-dwm-mobile.png"
        private val testBinaryFileName = "my_image.png"
    }


}