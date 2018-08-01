package com.criptext.mail.scenes.mailbox.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.androidtest.TestSharedPrefs
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.data.SaveEmailWorker
import com.criptext.mail.signal.*
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import io.mockk.mockk
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by gabriel on 6/30/18.
 */

@RunWith(AndroidJUnit4::class)
class SendEmailWorkerTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var signalClient: SignalClient
    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private lateinit var tester: DummyUser

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Phone)
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")
    private val bobContact = Contact(email = "bob@jigl.com", name = "Bob", id = 1)
    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.contactDao().insertIgnoringConflicts(bobContact)

        mailboxLocalDB = MailboxLocalDB.Default(db)
        signalClient = SignalClient.Default(store = SignalStoreCriptext(db))

        // create tester user so that signal store is initialized.
        val storage = TestSharedPrefs(mActivityRule.activity)
        tester = InDBUser(db = db, storage = storage, generator = keyGenerator,
                recipientId = "tester", deviceId = 1).setup()

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
    }

    private fun newWorker(emailId: Long, threadId: String?, inputData: ComposerInputData): SendMailWorker =
        SendMailWorker(signalClient = signalClient, emailId = emailId, threadId = threadId,
                rawSessionDao = db.rawSessionDao(), httpClient = httpClient, db = mailboxLocalDB,
                composerInputData = inputData, activeAccount = activeAccount,
                attachments = emptyList(), publishFn = {}, fileKey = null, rawIdentityKeyDao = db.rawIdentityKeyDao())



    private fun newSaveEmailWorker(inputData: ComposerInputData): SaveEmailWorker =
            SaveEmailWorker(composerInputData = inputData, emailId = null, threadId = null,
                    attachments = emptyList(), onlySave = false, account = activeAccount,
                    dao = db.emailInsertionDao(),  publishFn = {}, fileKey = null)


    private fun getDecryptedBodyPostEmailRequestBody(recipient: DummyUser): String {
        mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        val req = mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        val bodyString = req.body.readUtf8()
        val bodyJSON = JSONObject(bodyString)

        val firstCriptextEmail = bodyJSON.getJSONArray("criptextEmails").getJSONObject(0)
        val encryptedText = firstCriptextEmail.getString("body")

        return recipient.decrypt("tester", 1,
                SignalEncryptedData(encryptedB64 = encryptedText,
                        type = SignalEncryptedData.Type.preKey)
        )

    }

    @Test
    fun should_correctly_encrypt_an_email_and_send_it_to_an_inmemory_user() {
        // create in memory user that will receive and decrypt the email
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()

        // first we need to store the email to send in the DB
        val newComposedData = ComposerInputData(to = listOf(bobContact), cc = emptyList(),
                bcc = emptyList(), subject = "Test Message", body = "Hello Bob!", passwordForNonCriptextUsers = null)
        val saveEmailWorker = newSaveEmailWorker(newComposedData)
        val saveResult = saveEmailWorker.work(mockk(relaxed = true)) as ComposerResult.SaveEmail.Success

        // prepare server mocks to send email
        val keyBundleFromBob = bob.fetchAPreKeyBundle()
        val findKeyBundlesResponse = "[${keyBundleFromBob.toJSON()}]"
        val postEmailResponse = SentMailData(date = "2018-06-18 15:22:21", metadataKey = 1011,
                messageId = "__MESSAGE_ID__", threadId = "__THREAD_ID__").toJSON().toString()
        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok(findKeyBundlesResponse), /* /keybundle/find */
                MockedResponse.Ok(postEmailResponse) /* /email */
        ))

        // now send it
        val sendEmailWorker = newWorker(emailId = saveResult.emailId, threadId = saveResult.threadId,
                inputData = saveResult.composerInputData)
        sendEmailWorker.work(mockk(relaxed = true)) as MailboxResult.SendMail.Success

        // assert that bob could decrypt it
        val decryptedText = getDecryptedBodyPostEmailRequestBody(bob)
        decryptedText `shouldEqual` "Hello Bob!"
    }

    @Test
    fun should_update_the_email_in_db_with_the_result_of_the_http_request() {
        // create in memory user that will receive and decrypt the email
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()

        // first we need to store the email to send in the DB
        val newComposedData = ComposerInputData(to = listOf(bobContact), cc = emptyList(),
                bcc = emptyList(), subject = "Test Message", body = "Hello Bob!", passwordForNonCriptextUsers = null)
        val saveEmailWorker = newSaveEmailWorker(newComposedData)
        val saveResult = saveEmailWorker.work(mockk(relaxed = true)) as ComposerResult.SaveEmail.Success

        // prepare server mocks to send email
        val keyBundleFromBob = bob.fetchAPreKeyBundle()
        val findKeyBundlesResponse = "[${keyBundleFromBob.toJSON()}]"
        val postEmailResponse = SentMailData(date = "2018-06-18 15:22:21", metadataKey = 1011,
                messageId = "__MESSAGE_ID__", threadId = "__THREAD_ID__").toJSON().toString()
        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok(findKeyBundlesResponse), /* /keybundle/find */
                MockedResponse.Ok(postEmailResponse) /* /email */
        ))

        // now send it
        val sendEmailWorker = newWorker(emailId = saveResult.emailId, threadId = saveResult.threadId,
                inputData = saveResult.composerInputData)
        sendEmailWorker.work(mockk(relaxed = true)) as MailboxResult.SendMail.Success

        // assert that email got updated correctly in DB
        val updatedEmails = db.emailDao().getAll()

        val sentEmail = updatedEmails.single()
        sentEmail.delivered `shouldBe` DeliveryTypes.SENT
        sentEmail.metadataKey `shouldEqual` 1011L
        sentEmail.messageId `shouldEqual` "__MESSAGE_ID__"
        sentEmail.threadId `shouldEqual` "__THREAD_ID__"

    }

}