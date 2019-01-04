package com.criptext.mail.scenes.emaildetail.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.emaildetail.workers.ReadEmailsWorker
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.sql.Timestamp

@RunWith(AndroidJUnit4::class)
class ReadEmailsWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private val mockedThreadId = Timestamp(System.currentTimeMillis()).toString()
    private lateinit var db: TestDatabase
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "")

    private lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        storage = mockk(relaxed = true)
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServer.url("/mock").toString(), connectionTimeout = 7000L,
                readTimeout = 7000L)
    }

    @After
    fun teardown() {
        mockWebServer.close()
    }

    private fun newWorker(emailIds: List<Long>, metadataKeys: List<Long>): ReadEmailsWorker =
            ReadEmailsWorker(dao = db.emailDao(), activeAccount = activeAccount,
                    httpClient = httpClient, publishFn = {}, emailIds = emailIds,
                    metadataKeys = metadataKeys, pendingDao = db.pendingEventDao(), accountDao = db.accountDao(),
                    storage = storage)


    @Test
    fun should_send_request_to_read_any_unread_emails_and_return_the_number_of_updated_emails() {


        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("Ok"),
                MockedResponse.Ok("Ok")
        ))


        val loadedEmails = createEmailItemsInThread(4)
                .mapIndexed { index, fullEmail ->
                    // only the latter half are unread
                    if (index < 2) fullEmail.email.unread = true
                    fullEmail
                }
        db.emailDao().insertAll(loadedEmails.map { it.email })

        val worker = newWorker(loadedEmails.map { it.email.id }, loadedEmails.map { it.email.metadataKey })

        val result = worker.work(mockk(relaxed = true))
                as EmailDetailResult.ReadEmails.Success

        val unreadEmailsBefore = loadedEmails.filter { it.email.unread }
        result.readEmailsQuantity shouldEqualTo unreadEmailsBefore.size

    }
    private fun createEmailItemsInThread(size: Int): List<FullEmail> {
        return (1..size).map {
            FullEmail(
                    email = Email(id = it.toLong(),
                            content = """
                             <!DOCTYPE html>
                                <html>
                                <body>
                                    <h1>My $it Heading</h1>
                                    <p>My $it paragraph.</p>
                                </body>
                            </html>
                        """.trimIndent(),
                            date = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            delivered = DeliveryTypes.READ,
                            messageId = "key $it",
                            preview = "bodyPreview $it" ,
                            secure = true,
                            subject = "Subject $it",
                            threadId = mockedThreadId,
                            metadataKey = it + 100L,
                            unread = false,
                            isMuted = false,
                            unsentDate = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            trashDate = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null)),
                    labels = emptyList(),
                    to = emptyList(),
                    files = arrayListOf(CRFile(id = 0, token = "efhgfdgdfsg$it",
                            name = "test.pdf",
                            size = 65346L,
                            status = 1,
                            date = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            readOnly = false,
                            emailId = it.toLong(),
                            shouldDuplicate = false,
                            fileKey = "__FILE_KEY__"
                    )),
                    cc = emptyList(),
                    bcc = emptyList(),
                    from = Contact(1,"mayer@jigl.com", "Mayer Mizrachi"),
                    fileKey = null)
        }.reversed()
    }
}