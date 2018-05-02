package com.email.scenes.mailbox

import com.email.IHostActivity
import com.email.api.ApiCall
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.data.MailboxAPIClient
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.signal.SignalClient
import com.email.websocket.WebSocketEventListener
import com.email.websocket.WebSocketEventPublisher
import io.mockk.*
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/2/18.
 */
class MailboxWebSocketTest {
    private lateinit var model: MailboxSceneModel
    private lateinit var scene: MailboxScene
    private lateinit var signal: SignalClient
    private lateinit var db: MailboxLocalDB
    private lateinit var rawSessionDao: RawSessionDao
    private lateinit var emailInsertionDao: EmailInsertionDao
    private lateinit var api: MailboxAPIClient
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: MailboxDataSource
    private lateinit var controller: MailboxSceneController
    private lateinit var host: IHostActivity
    private lateinit var webSocketEvents: WebSocketEventPublisher
    private lateinit var webSocketListenerSlot: CapturingSlot<WebSocketEventListener>
    private lateinit var feedController : FeedController
    private lateinit var server : MockWebServer

    @Before
    fun setUp() {
        model = MailboxSceneModel()
        scene = mockk(relaxed = true)

        runner = MockedWorkRunner()
        db = mockk(relaxed = true)
        rawSessionDao = mockk()

        emailInsertionDao = mockk(relaxed = true)
        val runnableSlot = CapturingSlot<Runnable>() // run transactions as they are invoked
        every { emailInsertionDao.runTransaction(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
        }

        api = MailboxAPIClient("__JWT_TOKEN")
        signal = mockk()

        host = mockk()

        dataSource = MailboxDataSource(
                runner = runner,
                signalClient = signal,
                mailboxLocalDB = db,
                activeAccount = ActiveAccount("gabriel", "__JWT_TOKEN__"),
                rawSessionDao = rawSessionDao,
                emailInsertionDao = emailInsertionDao
        )

        feedController = mockk(relaxed = true)

        // capture web socket event listener
        webSocketListenerSlot = CapturingSlot()
        webSocketEvents = mockk(relaxed = true)
        every { webSocketEvents::listener.set(capture(webSocketListenerSlot)) } just Runs

        controller = MailboxSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  host,
                feedController = feedController,
                websocketEvents = webSocketEvents
        )

        server = MockWebServer()
        ApiCall.baseUrl = server.url("v1/mock").toString()
    }

    @Test
    fun `should reset the mailbox when an email for inbox arrives`() {
        controller.onStart(null)

        // skip all initialization
        runner.discardPendingWork()

        // mock database result
        every {
            db.getEmailsFromMailboxLabel(MailFolders.INBOX, null, 20, any())
        } returns MailboxTestUtils.createEmailThreads(20)

        // set 2 pages of threads
        model.threads.addAll(MailboxTestUtils.createEmailThreads(40))

        // create new email to "send" through web socket
        val newEmail = MailboxTestUtils.createEmailThreads(1).first().latestEmail.email
        newEmail.subject = "New real time email"
        newEmail.threadid = "__THREAD_ID__"


        // trigger socket event
        webSocketListenerSlot.captured.onNewEmail(newEmail)


        // assert that model has not updated until async task finishes
        model.threads.size `should be` 40

        // async work done
        runner._work()

        // should have removed the previous 40 and added the 20 loaded from db
        model.threads.size `should be` 20
    }
}