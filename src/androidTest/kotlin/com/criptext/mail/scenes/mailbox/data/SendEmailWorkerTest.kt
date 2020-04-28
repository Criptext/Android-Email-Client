package com.criptext.mail.scenes.mailbox.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.*
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.composer.data.ComposerAttachment
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.workers.SaveEmailWorker
import com.criptext.mail.scenes.mailbox.workers.SendMailWorker
import com.criptext.mail.signal.*
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.MockedResponse
import com.criptext.mail.utils.enqueueResponses
import io.mockk.*
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.whispersystems.libsignal.SignalProtocolAddress
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by gabriel on 6/30/18.
 */

@RunWith(AndroidJUnit4::class)
class SendEmailWorkerTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var composerLocalDB: ComposerLocalDB
    private lateinit var signalClient: SignalClient
    private lateinit var mockWebServer: MockWebServer
    private lateinit var httpClient: HttpClient
    private lateinit var tester: DummyUser

    private val signalAddresses = mutableListOf<List<SignalProtocolAddress>>()

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)
    private var activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD)
    private val bobContact = Contact(email = "bob@criptext.com", name = "Bob", id = 1,
            isTrusted = true, score = 0, spamScore = 0)
    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.contactDao().insertIgnoringConflicts(bobContact)

        composerLocalDB = ComposerLocalDB(db.contactDao(), db.emailDao(), db.fileDao(),
                db.fileKeyDao(), db.labelDao(), db.emailLabelDao(), db.emailContactDao(), db.accountDao(), db.aliasDao(),  mActivityRule.activity.filesDir)
        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        signalClient = SignalClient.Default(store = SignalStoreCriptext(db))

        // create tester user so that signal store is initialized.
        storage = mockk(relaxed = true)
        tester = InDBUser(db = db, storage = storage, generator = keyGenerator,
                recipientId = "tester", deviceId = 1).setup()
        activeAccount = activeAccount.copy(id = db.accountDao().getLoggedInAccount()!!.id)

        // mock http requests
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val mockWebServerUrl = mockWebServer.url("/mock").toString()
        httpClient = HttpClient.Default(authScheme = HttpClient.AuthScheme.jwt,
                baseUrl = mockWebServerUrl, connectionTimeout = 1000L, readTimeout = 1000L)
    }

    private fun newWorker(emailId: Long, threadId: String?, inputData: ComposerInputData,
                          attachments: List<ComposerAttachment>, fileKey: String?): SendMailWorker =
            SendMailWorker(signalClient = signalClient, emailId = emailId, threadId = threadId,
                    rawSessionDao = db.rawSessionDao(), httpClient = httpClient, db = mailboxLocalDB,
                    composerInputData = inputData, activeAccount = activeAccount,
                    attachments = attachments, publishFn = {}, fileKey = fileKey, rawIdentityKeyDao = db.rawIdentityKeyDao(),
                    accountDao = db.accountDao(), storage = storage, filesDir = mActivityRule.activity.filesDir,
                    currentLabel = Label.defaultItems.inbox, senderAddress = activeAccount.userEmail)



    private fun newSaveEmailWorker(inputData: ComposerInputData): SaveEmailWorker =
            SaveEmailWorker(composerInputData = inputData, emailId = null, threadId = null,
                    attachments = inputData.attachments!!, onlySave = false, senderAddress = activeAccount.userEmail,
                    dao = db.emailInsertionDao(), publishFn = {}, fileKey = inputData.fileKey, originalId = null,
                    filesDir = mActivityRule.activity.filesDir, currentLabel = Label.defaultItems.inbox,
                    db = composerLocalDB, activeAccount = activeAccount)


    private fun getDecryptedBodyPostEmailRequestBody(recipient: DummyUser): String {
        mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        val req = mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        val bodyString = req.body.readUtf8()
        val bodyJSON = JSONObject(bodyString)

        val firstCriptextEmail = bodyJSON.getJSONArray("criptextEmails").getJSONObject(0)
                .getJSONArray("emails").getJSONObject(0)
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
                bcc = emptyList(), subject = "Test Message", body = "Hello Bob!",
                attachments = ArrayList() , fileKey = null)
        val saveEmailWorker = newSaveEmailWorker(newComposedData)
        val saveResult = saveEmailWorker.work(mockk(relaxed = true)) as ComposerResult.SaveEmail.Success

        // prepare server mocks to send email
        val keyBundleFromBob = bob.fetchAPreKeyBundle(activeAccount.id)
        val jsonFindKeyBundleResponse = JSONObject()
        jsonFindKeyBundleResponse.put("keyBundles", JSONArray().put(keyBundleFromBob.toJSON()))
        jsonFindKeyBundleResponse.put("blacklistedKnownDevices", JSONArray())
        jsonFindKeyBundleResponse.put("guestDomains", JSONArray())
        jsonFindKeyBundleResponse.put("addresses", JSONArray())
        val postEmailResponse = SentMailData(date = "2018-06-18 15:22:21", metadataKey = 1011,
                messageId = "__MESSAGE_ID__", threadId = "__THREAD_ID__").toJSON().toString()
        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok(jsonFindKeyBundleResponse.toString()), /* /keybundle/find */
                MockedResponse.Ok(postEmailResponse) /* /email */
        ))

        // now send it
        val sendEmailWorker = newWorker(emailId = saveResult.emailId, threadId = saveResult.threadId,
                inputData = saveResult.composerInputData, attachments = emptyList(), fileKey = null)
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
                bcc = emptyList(), subject = "Test Message", body = "Hello Bob!",
                attachments = ArrayList(), fileKey = null)
        val saveEmailWorker = newSaveEmailWorker(newComposedData)
        val saveResult = saveEmailWorker.work(mockk(relaxed = true)) as ComposerResult.SaveEmail.Success

        // prepare server mocks to send email
        val keyBundleFromBob = bob.fetchAPreKeyBundle(activeAccount.id)
        val jsonFindKeyBundleResponse = JSONObject()
        jsonFindKeyBundleResponse.put("keyBundles", JSONArray().put(keyBundleFromBob.toJSON()))

        //Add blacklisted devices to test for removal
        val jsonBlacklistObject = JSONObject()
        jsonBlacklistObject.put("name", "jose")
        jsonBlacklistObject.put("domain", "criptext.com")
        jsonBlacklistObject.put("devices", JSONArray().put(1))
        jsonFindKeyBundleResponse.put("blacklistedKnownDevices", JSONArray().put(jsonBlacklistObject))
        jsonFindKeyBundleResponse.put("guestDomains", JSONArray())
        jsonFindKeyBundleResponse.put("addresses", JSONArray())
        val postEmailResponse = SentMailData(date = "2018-06-18 15:22:21", metadataKey = 1011,
                messageId = "__MESSAGE_ID__", threadId = "__THREAD_ID__").toJSON().toString()
        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok(jsonFindKeyBundleResponse.toString()), /* /keybundle/find */
                MockedResponse.Ok(postEmailResponse) /* /email */
        ))

        // now send it
        val sendEmailWorker = newWorker(emailId = saveResult.emailId, threadId = saveResult.threadId,
                inputData = saveResult.composerInputData, attachments = emptyList(), fileKey = null)
        sendEmailWorker.work(mockk(relaxed = true)) as MailboxResult.SendMail.Success

        // assert that email got updated correctly in DB
        val updatedEmails = db.emailDao().getAll(activeAccount.id)

        val sentEmail = updatedEmails.single()
        sentEmail.delivered `shouldBe` DeliveryTypes.SENT
        sentEmail.metadataKey `shouldEqual` 1011L
        sentEmail.messageId `shouldEqual` "__MESSAGE_ID__"
        sentEmail.threadId `shouldEqual` "__THREAD_ID__"

    }

    @get:Rule
    var folder = TemporaryFolder()

    @Test
    fun should_send_same_number_of_attachments_as_the_email_has(){
        // create in memory user that will receive and decrypt the email
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()
        // create the Composer Attachment list with mock files
        val attachmentList = ArrayList<ComposerAttachment>()
        for (i in 1..4){
            val file = folder.newFile(String.format("file_%s.png", i))
            attachmentList.add(ComposerAttachment(
                    id=0,
                    uuid = UUID.randomUUID().toString(),
                    filepath=file.name,
                    uploadProgress=100,
                    filetoken="__FILE_TOKEN__",
                    type= AttachmentTypes.IMAGE,
                    size=file.totalSpace,
                    fileKey = "__FILE_KEY__",
                    cid = null
            ))
        }
        // first we need to store the email to send in the DB
        val newComposedData = ComposerInputData(to = listOf(bobContact), cc = emptyList(),
                bcc = emptyList(), subject = "Test Message", body = "",
                attachments = attachmentList, fileKey = "__FILE_KEY__")
        val saveEmailWorker = newSaveEmailWorker(newComposedData)
        val saveResult = saveEmailWorker.work(mockk(relaxed = true)) as ComposerResult.SaveEmail.Success

        // prepare server mocks to send email
        val keyBundleFromBob = bob.fetchAPreKeyBundle(activeAccount.id)
        val jsonFindKeyBundleResponse = JSONObject()
        jsonFindKeyBundleResponse.put("keyBundles", JSONArray().put(keyBundleFromBob.toJSON()))
        jsonFindKeyBundleResponse.put("blacklistedKnownDevices", JSONArray())
        jsonFindKeyBundleResponse.put("guestDomains", JSONArray())
        jsonFindKeyBundleResponse.put("addresses", JSONArray())
        val postEmailResponse = SentMailData(date = "2018-06-18 15:22:21", metadataKey = 1011,
                messageId = "__MESSAGE_ID__", threadId = "__THREAD_ID__").toJSON().toString()
        mockWebServer.enqueueResponses(listOf(
                MockedResponse.Ok(jsonFindKeyBundleResponse.toString()), /* /keybundle/find */
                MockedResponse.Ok(postEmailResponse) /* /email */
        ))

        // now send it
        val sendEmailWorker = newWorker(emailId = saveResult.emailId, threadId = saveResult.threadId,
                inputData = saveResult.composerInputData, attachments = saveResult.attachments, fileKey = saveResult.fileKey)
        sendEmailWorker.work(mockk(relaxed = true)) as MailboxResult.SendMail.Success
        // get request from mockwebserver
        mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        val req = mockWebServer.takeRequest(0, java.util.concurrent.TimeUnit.HOURS)
        // get send email
        val bodyString = req.body.readUtf8()
        val bodyJSON = JSONObject(bodyString)
        val filesEmailSend = bodyJSON.getJSONArray("files")
        // compare send files with save files
        saveResult.attachments.size `shouldEqual` 4
        saveResult.attachments.size `shouldEqual` filesEmailSend.length()
    }

}