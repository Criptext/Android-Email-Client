package com.email.scenes.mailbox.data

import com.email.api.HttpClient
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.mocks.MockedJSONData
import com.email.scenes.mailbox.MailboxTestUtils
import com.email.signal.SignalClient
import com.email.utils.file.hasArray
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldBeEmpty
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException

/**
 * Created by gabriel on 5/7/18.
 */
class UpdateMailboxWorkerTest {

    private lateinit var signal: SignalClient
    private lateinit var httpClient: HttpClient
    private lateinit var db: MailboxLocalDB
    private lateinit var dao: EmailInsertionDao
    private lateinit var activeAccount: ActiveAccount

    @Before
    fun setup() {
        activeAccount = ActiveAccount(recipientId = "gabriel", jwt = "__JWTOKEN__")
        signal = mockk()
        db = mockk()
        dao = mockk(relaxed = true)
        val runnableSlot = CapturingSlot<Runnable>() // run transactions as they are invoked
        every { dao.runTransaction(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
        }

        httpClient = mockk()
    }

    private fun newWorker(loadedThreadsCount: Int, label: Label): UpdateMailboxWorker =
        UpdateMailboxWorker(signalClient = signal, db = db, dao = dao, httpClient = httpClient,
                activeAccount = activeAccount, publishFn = {},
                loadedThreadsCount = loadedThreadsCount, label = label)

    @Test
    fun `should request events and insert new emails and if no errors should acknowledge all events`() {
        val label = Label.defaultItems.inbox
        val worker = newWorker(20, label)

        every {
            httpClient.get(url = "/event", jwt = "__JWTOKEN__")
        } returns MockedJSONData.sample2NewEmailEvents
        every {
            httpClient.get(url = match { it.startsWith("/email") }, jwt = "__JWTOKEN__")
        } returnsMany listOf("__ENCRYPTED_TEXT_1__", "__ENCRYPTED_TEXT_2__")
        every {
            httpClient.post(url = "/event/ack", jwt = "__JWTOKEN__",
                    body = match { it.hasArray("ids").ofLength(2) })
        } returns "OK"

        // prepare db mocks
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = label.text, oldestEmailThread = null,
                    limit = 20, rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label))
        } returns MailboxTestUtils.createEmailThreads(20)
        val insertedEmails = mutableListOf<Email>()
        every {
            dao.insertEmail(capture(insertedEmails))
        } returnsMany listOf(1L, 2L)
        every {
            dao.findEmailByMessageId(any())
        } returns null
        every {
            dao.findContactsByEmail(any())
        } returns emptyList()
        every {
            dao.insertContacts(any())
        } returns listOf(1L, 2L)

        // prepare signal mocks
        every {
            signal.decryptMessage(recipientId = "mayer", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_1__")
        } returns "__PLAIN_TEXT_1__"
        every {
            signal.decryptMessage(recipientId = "gianni", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_2__")
        } returns "__PLAIN_TEXT_2__"

        val result = worker.work() as MailboxResult.UpdateMailbox.Success

        // server.requestCount `should equal` 4
        insertedEmails.map { Pair(it.subject, it.content) } `should equal` listOf(
                Pair("hello", "__PLAIN_TEXT_1__"),
                Pair("hello again", "__PLAIN_TEXT_2__")) // decrypted and inserted everything
        result.mailboxThreads!!.size `should equal` 20 // should reload with new emails


    }

    @Test
    fun `should request events and if body requests fail, should not acknowledge those events`() {
        val label = Label.defaultItems.inbox
        val worker = newWorker(20, label)

        // mock server responses. Only one, assume all others timeout
        every {
            httpClient.get(url = "/event", jwt = "__JWTOKEN__")
        } returns MockedJSONData.sample2NewEmailEvents
        every {
            httpClient.get(url = match { it.startsWith("/email") }, jwt = "__JWTOKEN__")
        } throws SocketTimeoutException()

        // prepare db mocks
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = label.text, oldestEmailThread = null,
                    limit = 20, rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label))
        } returns MailboxTestUtils.createEmailThreads(20)
        val insertedEmails = mutableListOf<Email>()
        every {
            dao.insertEmail(capture(insertedEmails))
        } returnsMany listOf(1L, 2L)
        every {
            dao.findEmailByMessageId(any())
        } returns null
        every {
            dao.findContactsByEmail(any())
        } returns emptyList()
        every {
            dao.insertContacts(any())
        } returns listOf(1L, 2L)

        // prepare signal mocks
        every {
            signal.decryptMessage(recipientId = "mayer", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_1__")
        } returns "__PLAIN_TEXT_1__"
        every {
            signal.decryptMessage(recipientId = "gianni", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_2__")
        } returns "__PLAIN_TEXT_2__"

        val result = worker.work() as MailboxResult.UpdateMailbox.Success

        result.mailboxThreads `should be` null // nothing to update
        insertedEmails.shouldBeEmpty() // nothing got inserted
    }

    fun JSONObject.hasArrayOfSize(size: Int) = this.length() == size

    @Test
    fun `should request events and if new mails were already in db, should acknowledge those events`() {
        val label = Label.defaultItems.inbox
        val worker = newWorker(20, label)

        // mock server responses. No body requests since mails are already in db
        every {
            httpClient.get(url = "/event", jwt = "__JWTOKEN__")
        } returns MockedJSONData.sample2NewEmailEvents
        every {
            httpClient.post(url = "/event/ack", jwt = "__JWTOKEN__",
                    body = match { it.hasArray("ids").ofLength(2) })
        } throws SocketTimeoutException()

        // prepare db mocks
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = label.text, oldestEmailThread = null,
                    limit = 20, rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(label))
        } returns MailboxTestUtils.createEmailThreads(20)
        val insertedEmails = mutableListOf<Email>()
        every {
            dao.insertEmail(capture(insertedEmails))
        } returnsMany listOf(1L, 2L)
        every {
            dao.findEmailByMessageId(any())
        } returns MailboxTestUtils.createNewEmail(1) // mails already exist
        every {
            dao.findContactsByEmail(any())
        } returns emptyList()
        every {
            dao.insertContacts(any())
        } returns listOf(1L, 2L)

        // prepare signal mocks
        every {
            signal.decryptMessage(recipientId = "mayer", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_1__")
        } returns "__PLAIN_TEXT_1__"
        every {
            signal.decryptMessage(recipientId = "gianni", deviceId = 1, encryptedB64 = "__ENCRYPTED_TEXT_2__")
        } returns "__PLAIN_TEXT_2__"

        worker.work() as MailboxResult.UpdateMailbox.Success

        insertedEmails.shouldBeEmpty() // nothing got inserted
    }
}