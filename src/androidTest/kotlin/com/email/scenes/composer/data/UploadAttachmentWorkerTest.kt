package com.email.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.HttpClient
import com.email.db.models.Label
import com.email.signal.Encoding
import com.email.utils.FileDownloader
import com.email.utils.file.AndroidFs
import org.junit.AfterClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class UploadAttachmentWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private val fileServiceAuthToken =
            Encoding.byteArrayToString(
                    "qynhtyzjrshazxqarkpy:lofjksedbxuucdjjpnby".toByteArray(
                            Charset.forName("UTF-8")
                    )
            )

    private lateinit var httpClient: HttpClient


    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)

        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.basic,
                baseUrl = "http://services.criptext.com", connectionTimeout = 7000L,
                readTimeout = 7000L)
    }

    private fun newWorker(filepath: String): UploadAttachmentWorker =
            UploadAttachmentWorker(filepath = filepath, fileServiceAuthToken = fileServiceAuthToken,
                    httpClient = httpClient, publishFn = {})

    @Test
    fun should_upload_file_without_errors() {

        val fileToUpload = AndroidFs.getFileFromImageCache(mActivityRule.activity,
                testBinaryFileName)
        try {
            FileDownloader.download(testBinaryFileURL, fileToUpload)

            val worker = newWorker(fileToUpload.absolutePath)

            worker.work() as ComposerResult.UploadFile.Success
        } finally {
            fileToUpload.delete()
        }

    }

    companion object {
        private val testBinaryFileURL = "https://cdn.criptext.com/Email/images/emailhome/icon-dwm-mobile.png"
        private val testBinaryFileName = "my_image.png"
    }


}