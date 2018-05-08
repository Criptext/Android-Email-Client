package com.email.websocket

import com.email.api.ApiCall
import com.email.api.EmailInsertionAPIClient
import com.email.api.HttpClient
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.mocks.MockedJSONData
import com.email.mocks.MockedWorkRunner
import com.email.signal.SignalClient
import com.email.websocket.data.EventDataSource
import com.email.websocket.data.InsertNewEmailWorker
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/1/18.
 */

class WebSocketTests {
    private lateinit var signal: SignalClient
    private lateinit var httpClient: HttpClient
    private lateinit var dao: EmailInsertionDao
    private lateinit var api: EmailInsertionAPIClient
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: EventDataSource
    private lateinit var controller: WebSocketController
    private lateinit var webSocket: WebSocketClient
    private lateinit var server : MockWebServer

    private val onMessageReceivedSlot = CapturingSlot<(String)->Unit>()

    @Before
    fun setUp() {
        dao = mockk(relaxed = true)
        val runnableSlot = CapturingSlot<Runnable>() // run transactions as they are invoked
        every { dao.runTransaction(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
        }

        signal = mockk()
        server = MockWebServer()
        httpClient = HttpClient.Default(baseUrl = server.url("v1/mock").toString(),
                connectionTimeout = 1000L, readTimeout = 1000L)
        api = EmailInsertionAPIClient(httpClient,"__JWT_TOKEN__")

        runner = MockedWorkRunner()

        dataSource = EventDataSource(runner = runner, emailInsertionDao = dao,
                emailInsertionAPIClient = api, signalClient = signal)

        webSocket = mockk()
        every { webSocket.connect(any(), capture(onMessageReceivedSlot))} just Runs

        val account = ActiveAccount(recipientId = "tester", jwt = "__JWT_TOKEN__")
        controller = WebSocketController(wsClient = webSocket, activeAccount = account,
                eventDataSource = dataSource)

    }

    @Test
    fun `when socket receives a new email, should store it in db and invoke the event listener`() {
        var didInsertSender = false
        val mockedListener: WebSocketEventListener = mockk(relaxed = true)
        val insertedEmailSlot  = CapturingSlot<Email>()
        val newEmailSlot  = CapturingSlot<Email>()

        // prepare mocks
        every {
            signal.decryptMessage("mayer", 1, "__ENCRYPTED_TEXT__")
        } returns "__PLAIN_TEXT__"

        every { dao.findContactsByEmail(listOf("mayer@jigl.com")) } returns emptyList()
        every {
            dao.findContactsByEmail(listOf("gabriel@jigl.com"))
            } returns listOf(Contact(id = 0, email ="gabriel@jigl.com", name = "Gabriel Aumala"))
        every {
            dao.insertContacts(listOf(Contact(0, "mayer@jigl.com",
                    "Mayer Mizrachi")))
        } answers { didInsertSender = true; listOf(2) }

        every {
            dao.insertEmail(capture(insertedEmailSlot))
        } returns 6
        every {
            dao.findEmailByMessageId("<15221916.12518@jigl.com>")
        } answers { if (insertedEmailSlot.isCaptured) insertedEmailSlot.captured else null }
        every {
            mockedListener.onNewEmail(capture(newEmailSlot))
        } just Runs

        // mock server response
        server.enqueue(MockResponse()
                .setBody("__ENCRYPTED_TEXT__")
                .setResponseCode(200))


        controller.listener = mockedListener

        val onMessageReceived = onMessageReceivedSlot.captured
        onMessageReceived(MockedJSONData.sampleNewEmailEvent) // trigger new message event

        runner.assertPendingWork(listOf(InsertNewEmailWorker::class.java))
        runner._work() // trigger async work done

        didInsertSender `should be` true

        // assert that listener got the latest inserted email
        val newEmailReceivedByListener = newEmailSlot.captured
        val newInsertedEmail = insertedEmailSlot.captured

        newEmailReceivedByListener `should be` newInsertedEmail
        newEmailReceivedByListener.content `should equal` "__PLAIN_TEXT__"
    }
}