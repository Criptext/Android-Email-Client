package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.db.DeliveryTypes
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.KnownAddress
import com.email.db.models.signal.CRPreKey
import com.email.scenes.composer.data.ComposerInputData
import com.email.signal.PreKeyBundleShareData
import com.email.signal.SignalClient
import com.email.signal.SignalEncryptedData
import com.gaumala.kotlinsnapshot.Camera
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.`should be instance of`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.concurrent.thread

/**
 * Created by gabriel on 5/22/18.
 */
class SendEmailWorkerTest {
    private lateinit var signal: SignalClient
    private lateinit var httpClient: HttpClient
    private lateinit var db: MailboxLocalDB
    private lateinit var dao: RawSessionDao
    private lateinit var activeAccount: ActiveAccount
    private val camera = Camera()

    @Before
    fun setup() {
        activeAccount = ActiveAccount(recipientId = "gabriel", jwt = "__JWTOKEN__")
        signal = mockk(relaxed = true)
        db = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        httpClient = mockk()
    }

    private fun newWorker(emailId: Long, threadId: String?, inputData: ComposerInputData): SendMailWorker =
        SendMailWorker(signalClient = signal, emailId = emailId, threadId = threadId,
                rawSessionDao = dao, httpClient = httpClient, composerInputData = inputData,
                activeAccount = activeAccount, db = db, publishFn = {})

    private fun mockFindKeyBundlesResponse(): String {
        val shareData = PreKeyBundleShareData(recipientId = "mayer", deviceId = 1,
                registrationId = 1568, signedPreKeyId = 8,
                signedPreKeySignature = "__SIGNED_PRE_KEY_SIGNATURE__",
                signedPreKeyPublic = "__SIGNED_PRE_KEY_PUBLIC__",
                identityPublicKey = "__IDENTITY_PUBLIC_KEY__")
        val preKey = CRPreKey(id = 7, byteString = "__PRE_KEY_7__")
        val bundle = PreKeyBundleShareData.DownloadBundle(shareData = shareData, preKey = preKey)
        return "[${bundle.toJSON()}]"

    }

    private fun assertThatSendEmailHttpRequestsHaveCorrectShape(
            findKeybundlesBodySlot: CapturingSlot<JSONObject>,
            postEmailBodySlot: CapturingSlot<JSONObject>) {
        camera.matchWithSnapshot("should send find keybundles request with correct shape",
                findKeybundlesBodySlot.captured.toString(4))
        camera.matchWithSnapshot("should send post email request with correct shape",
                postEmailBodySlot.captured.toString(4))
    }

    @Test
    fun `should upload email contents so that the server can send it`() {
        val worker = newWorker(emailId = 2, threadId = null, inputData = ComposerInputData(
                to = listOf(Contact(id = 1, email = "mayer@jigl.com", name = "Mayer Mizrachi")),
                cc = emptyList(), bcc = emptyList(), subject = "Test E-mail",
                body = "Hello this is a test."
            ))

        val findKeybundlesBodySlot = CapturingSlot<JSONObject>()
        val postEmailBodySlot = CapturingSlot<JSONObject>()
        val mockedSentMailData = SentMailData(date = "2018-05-22 07:30:00", metadataKey = 7,
                messageId = "<124678.1267893@jigl.com>", threadId = "<124678.1267893@jigl.com>")

        every {
            signal.encryptMessage(recipientId = "mayer", deviceId = 1,
                    plainText = "Hello this is a test.")
        } returns SignalEncryptedData(encryptedB64 = "__ENCRYPTED_DATA__",
                type = SignalEncryptedData.Type.preKey)
        every {
            httpClient.post(path = "/keybundle/find", jwt = "__JWTOKEN__",
                    body = capture(findKeybundlesBodySlot))
        } returns mockFindKeyBundlesResponse()
        every {
            httpClient.post(path = "/email", jwt = "__JWTOKEN__",
                    body = capture(postEmailBodySlot))
        } returns mockedSentMailData.toJSON().toString()
        every {
            dao.getKnownAddresses(listOf("mayer"))
        } returns emptyList() andThen listOf(KnownAddress(recipientId = "mayer", deviceId = 1))


        worker.work() `should be instance of` MailboxResult.SendMail.Success::class.java

        assertThatSendEmailHttpRequestsHaveCorrectShape(findKeybundlesBodySlot, postEmailBodySlot)
        verify { // assert email got updated in db
            db.updateEmailAndAddLabelSent(id = 2, threadId = mockedSentMailData.threadId,
                key = "7", status = DeliveryTypes.SENT, date = any()
            )
        }
    }

}