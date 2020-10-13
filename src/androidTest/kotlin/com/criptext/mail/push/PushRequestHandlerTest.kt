package com.criptext.mail.push

import android.app.NotificationManager
import android.content.Context
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.androidtest.TestSharedPrefs
import com.criptext.mail.androidui.criptextnotification.NotificationError
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.*
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.push.data.PushAPIRequestHandler
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.sql.Timestamp

@RunWith(AndroidJUnit4::class)
class PushRequestHandlerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private val mockedThreadId = Timestamp(System.currentTimeMillis()).toString()
    private lateinit var db: TestDatabase
    private lateinit var mockWebServer: MockWebServer
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "__REFRESH__", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD, blockRemoteContent = true,
            defaultAddress = null)

    private lateinit var httpClient: HttpClient
    private lateinit var loadedEmails: List<FullEmail>
    private lateinit var emailDetailLocalDB: EmailDetailLocalDB
    private lateinit var storage: KeyValueStorage

    @Before
    fun setup() {
        storage = TestSharedPrefs(mActivityRule.activity)
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(id = 1, recipientId = "tester", deviceId = 1,
                name = "Tester", registrationId = 1, blockRemoteContent = true,
                identityKeyPairB64 = "_IDENTITY_", jwt = "__JWTOKEN__", type = AccountTypes.STANDARD,
                signature = "", refreshToken = "__REFRESH__", isActive = true, domain = "criptext.com", isLoggedIn = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null,
                defaultAddress = null))
        emailDetailLocalDB = EmailDetailLocalDB.Default(db, mActivityRule.activity.filesDir)
        mockWebServer = MockWebServer()
        mockWebServer.start()

        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServer.url("/mock").toString(), connectionTimeout = 7000L,
                readTimeout = 7000L)
        loadedEmails = createEmailItemsInThread(4)
                .mapIndexed { _, fullEmail ->
                    // only the latter half are unread
                    fullEmail.email.unread = true
                    fullEmail
                }
        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox),
                mActivityRule.activity.filesDir, activeAccount.recipientId, accountId = activeAccount.id,
                domain = activeAccount.domain)
    }

    @After
    fun teardown() {
        mockWebServer.close()
    }

    private fun newHandler(): PushAPIRequestHandler =
            PushAPIRequestHandler(not = NotificationError(mActivityRule.activity), activeAccount = activeAccount,
                    manager = mActivityRule.activity
                            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
                    httpClient = httpClient, storage = storage)

    @Test
    fun should_move_to_trash_the_email_shown_on_the_push_notification() {


        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("Ok")
        ))

        val requestHandler = newHandler()
        requestHandler.trashEmail(loadedEmails.last().email.metadataKey, 0,
                emailDetailLocalDB, db.emailDao(), db.pendingEventDao(), db.accountDao())

        val trashEmails = db.emailDao().getMetadataKeysFromLabel(Label.defaultItems.trash.id, activeAccount.id)


        trashEmails.size shouldEqualTo 1

    }


    @Test
    fun should_mark_as_read_the_email_shown_on_the_push_notification() {


        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok("Ok")
        ))

        val requestHandler = newHandler()
        requestHandler.openEmail(loadedEmails.last().email.metadataKey, 0, db.emailDao(),
                db.pendingEventDao(), db.accountDao())

        val readEmails = db.emailDao().getAll(activeAccount.id).filter { !it.unread }


        readEmails.size shouldEqualTo 1

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
                            unsentDate = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            trashDate = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            boundary = null,
                            replyTo = null,
                            fromAddress = "mayer@jigl.com",
                            accountId = activeAccount.id,
                            isNewsletter = null),
                    labels = emptyList(),
                    to = emptyList(),
                    files = arrayListOf(CRFile(id = 0, token = "efhgfdgdfsg$it",
                            name = "test.pdf",
                            size = 65346L,
                            status = 1,
                            date = DateAndTimeUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            emailId = it.toLong(),
                            shouldDuplicate = false,
                            fileKey = "__FILE_KEY__",
                            cid = null
                    )),
                    cc = emptyList(),
                    bcc = emptyList(),
                    from = Contact(1,"mayer@jigl.com", "Mayer Mizrachi", true, 0, 0),
                    fileKey = null,
                    headers = null)
        }.reversed()
    }
}