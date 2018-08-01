package com.email.scenes.mailbox

import com.email.IHostActivity
import com.email.SecureEmail
import com.email.api.HttpClient
import com.email.db.MailboxLocalDB
import com.email.db.dao.*
import com.email.db.dao.signal.RawIdentityKeyDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.ActiveAccount
import com.email.db.models.Label
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.data.LoadEmailThreadsWorker
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.signal.SignalClient
import com.email.websocket.WebSocketEventListener
import com.email.websocket.WebSocketEventPublisher
import io.mockk.*
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
    private lateinit var httpClient: HttpClient
    private lateinit var emailDao: EmailDao
    private lateinit var feedItemDao: FeedItemDao
    private lateinit var contactDao: ContactDao
    private lateinit var accountDao: AccountDao
    private lateinit var fileDao: FileDao
    private lateinit var fileKeyDao: FileKeyDao
    private lateinit var labelDao: LabelDao
    private lateinit var emailLabelDao: EmailLabelDao
    private lateinit var emailCOntactDao: EmailContactJoinDao
    private lateinit var rawSessionDao: RawSessionDao
    private lateinit var rawIdentityKeyDao: RawIdentityKeyDao
    private lateinit var emailInsertionDao: EmailInsertionDao
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: MailboxDataSource
    private lateinit var controller: MailboxSceneController
    private lateinit var host: IHostActivity
    private lateinit var webSocketEvents: WebSocketEventPublisher
    private lateinit var webSocketListenerSlot: CapturingSlot<WebSocketEventListener>
    private lateinit var feedController : FeedController
    private lateinit var activeAccount: ActiveAccount

    @Before
    fun setUp() {
        model = MailboxSceneModel()
        scene = mockk(relaxed = true)

        runner = MockedWorkRunner()
        db = mockk(relaxed = true)
        rawSessionDao = mockk()
        rawIdentityKeyDao = mockk()
        emailDao = mockk()
        feedItemDao = mockk()
        contactDao = mockk()
        fileDao = mockk()
        fileKeyDao = mockk()
        labelDao = mockk()
        emailLabelDao = mockk()
        emailCOntactDao = mockk()
        accountDao = mockk()

        emailInsertionDao = mockk(relaxed = true)
        val lambdaSlot = CapturingSlot<() -> Long>() // run transactions as they are invoked
        every { emailInsertionDao.runTransaction(capture(lambdaSlot)) } answers {
            lambdaSlot.captured()
        }

        signal = mockk()
        host = mockk()

        httpClient = mockk()

        activeAccount = ActiveAccount(name = "Gabriel", recipientId = "gabriel",
                deviceId = 3, jwt = "__JWT_TOKEN__", signature = "")

        dataSource = MailboxDataSource(
                runner = runner,
                signalClient = signal,
                httpClient = httpClient,
                mailboxLocalDB = db,
                activeAccount = activeAccount,
                emailDao = emailDao,
                contactDao = contactDao,
                feedItemDao = feedItemDao,
                rawSessionDao = rawSessionDao,
                rawIdentityKeyDao = rawIdentityKeyDao,
                emailInsertionDao = emailInsertionDao,
                fileDao = fileDao,
                fileKeyDao = fileKeyDao,
                labelDao = labelDao,
                emailLabelDao = emailLabelDao,
                emailContactJoinDao = emailCOntactDao,
                accountDao = accountDao
        )

        feedController = mockk(relaxed = true)

        // capture web socket event listener
        webSocketListenerSlot = CapturingSlot()
        webSocketEvents = mockk(relaxed = true)
        every { webSocketEvents.setListener(capture(webSocketListenerSlot)) } just Runs

        controller = MailboxSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  host,
                feedController = feedController,
                websocketEvents = webSocketEvents,
                activeAccount = activeAccount
        )

    }

    @Test
    fun `when new email arrives, should reset the mailbox`() {
        controller.onStart(null)

        // skip all initialization
        runner.discardPendingWork()

        // mock database result
        every {
            db.getThreadsFromMailboxLabel("gabriel@jigl.com",
                    SecureEmail.LABEL_INBOX, null, 20, any())
        } returns MailboxTestUtils.createEmailThreads(20)
        every {
            emailInsertionDao.findEmailByMessageId(any())
        } returns null

        // set 2 pages of threads
        model.threads.addAll(MailboxTestUtils.createEmailPreviews(40))

        // create new email to "send" through web socket
        val newEmail = MailboxTestUtils.createEmailThreads(1).first().latestEmail.email
        newEmail.subject = "New real time email"
        newEmail.threadId = "__THREAD_ID__"


        // trigger socket event
        webSocketListenerSlot.captured.onNewEmail(newEmail)


        // assert that model has not updated until async task finishes
        model.threads.size `should be` 40

        runner.assertPendingWork(listOf(LoadEmailThreadsWorker::class.java))
        // async work done
        runner._work(mockk())

        // should have removed the previous 40 and added the 20 loaded from db
        model.threads.size `should be` 20
    }

}