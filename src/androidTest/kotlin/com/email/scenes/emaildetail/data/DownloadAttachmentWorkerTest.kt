package com.email.scenes.emaildetail.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.Config
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.HttpClient
import com.email.bgworker.ProgressReporter
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.composer.data.UploadAttachmentWorker
import com.email.scenes.emaildetail.workers.DownloadAttachmentWorker
import com.email.signal.Encoding
import com.email.utils.*
import com.email.utils.file.AndroidFs
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
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class DownloadAttachmentWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mockWebServer: MockWebServer
    private val fileServiceAuthToken =
            Encoding.byteArrayToString(
                    "qynhtyzjrshazxqarkpy:lofjksedbxuucdjjpnby".toByteArray(
                            Charset.forName("UTF-8")
                    )
            )
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

        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.basic,
                baseUrl = getFilServiceBaseUrl(), connectionTimeout = 7000L,
                readTimeout = 7000L)
    }

    @After
    fun teardown() {
        if (Config.mockCriptextHTTPRequests)
            mockWebServer.close()
    }

    private fun newWorker(filepath: String): UploadAttachmentWorker =
            UploadAttachmentWorker(filepath = filepath, fileServiceAuthToken = fileServiceAuthToken,
                    httpClient = httpClient, publishFn = {})

    private fun newDownloadWorker(filetoken: String): DownloadAttachmentWorker =
            DownloadAttachmentWorker(fileToken = filetoken, emailId = 0,
                    downloadPath = mActivityRule.activity.cacheDir.absolutePath,
                    httpClient = httpClient, fileServiceAuthToken = fileServiceAuthToken,
                    publishFn = {})

    private fun sendPermanentRequest(filetoken: String){
        val filejson = JSONObject()
        filejson.put("token", filetoken)
        val json = JSONObject()
        val files = JSONArray()
        files.put(filejson)
        json.put("files", files)
        httpClient.post("/file/save", fileServiceAuthToken, json)
    }

    @Test
    fun should_download_file_without_errors() {

        val fileToUpload = AndroidFs.getFileFromImageCache(mActivityRule.activity,
                testBinaryFileName)

        if (Config.mockCriptextHTTPRequests) {
            mockWebServer.enqueueSuccessfulResponses(listOf(
                    """{"filetoken":"__FILETOKEN__"}""",
                    """{"filetoken":"__FILETOKEN__"}""",
                    """{"filetoken":"__FILETOKEN__"}""",
                    """{"file": {"token": "dsfdsfsda", "name": "test.pdf", "chunk_size": 512000, "chunks": 2}}""",
                    """{"filetoken":"__FILETOKEN__"}""",
                    """{"filetoken":"__FILETOKEN__"}"""
            ))
        }

        try {
            FileDownloader.download(testBinaryFileURL, fileToUpload)

            val worker = newWorker(fileToUpload.absolutePath)
            worker.work(reporter)
                    as ComposerResult.UploadFile.Success


            if(!Config.mockCriptextHTTPRequests) {
                sendPermanentRequest(filetoken)
                Thread.sleep(10000)
            }

            val downloadWorker = newDownloadWorker(filetoken)

            val result = downloadWorker.work(mockk(relaxed = true)) as EmailDetailResult.DownloadFile.Success

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