package com.email.scenes.mailbox

import com.email.IHostActivity
import com.email.api.ApiCall
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.*
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.data.*
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.ui.MailboxUIObserver
import com.email.signal.SignalClient
import com.email.websocket.WebSocketEventPublisher
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Created by gabriel on 4/26/18.
 */

class MailboxSceneControllerTest {
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
    private lateinit var feedController : FeedController
    private lateinit var server : MockWebServer

    private val threadEventListenerSlot = CapturingSlot<EmailThreadAdapter.OnThreadEventListener>()
    private val onDrawerMenuEventListenerSlot = CapturingSlot<DrawerMenuItemListener>()
    private val observerSlot = CapturingSlot<MailboxUIObserver>()

    @Before
    fun setUp() {
        model = MailboxSceneModel()
        // mock MailboxScene capturing the thread event listener
        scene = mockk(relaxed = true)
        every {
            scene.attachView(MailFolders.INBOX, capture(threadEventListenerSlot),
                    capture(onDrawerMenuEventListenerSlot), capture(observerSlot), any())
        } just Runs

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
        webSocketEvents = mockk(relaxed = true)
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

    private fun createEmailThreads(size: Int): List<EmailThread> {
        val dateMilis = System.currentTimeMillis()
        return (1..size)
                .map {
                    val email = Email(id = 0, key = it.toString(), threadid = "thread$it", unread = true,
                            secure = true, content = "this is message #$it", preview = "message #$it",
                            subject = "message #$it", delivered = DeliveryTypes.DELIVERED,
                            date = Date(dateMilis + it), isTrash = false, isDraft = false)
                    val fullEmail = FullEmail(email, labels = listOf(Label.defaultItems.inbox),
                            to = listOf(Contact(1, "gabriel@criptext.com", "gabriel")),
                                    cc = emptyList(), bcc = emptyList(), files = emptyList(),
                            from = Contact(2, "mayer@criptext.com", name = "Mayer"))
                    EmailThread(fullEmail, listOf(Label.defaultItems.inbox))
                }
    }
    @Test
    fun `should forward onStart and onStop to FeedController`() {
        controller.onStart(null)
        controller.onStop()

        verifySequence {
            feedController.onStart()
            feedController.onStop()
        }
    }

    fun afterFirstLoad(assertions: () -> Unit) {
        controller.onStart(null)
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = MailFolders.INBOX,
                    oldestEmailThread = null,
                    rejectedLabels = any(),
                    limit = 20)
        } returns createEmailThreads(20)

        runner.assertPendingWork(listOf(GetMenuInformationWorker::class.java,
                LoadEmailThreadsWorker::class.java))
        runner._work()
        runner._work()
        assertions()
    }

    @Test
    fun `onStart, should load threads if empty`() {
        afterFirstLoad {
            model.threads.size `should equal` 20
        }
    }

    @Test
    fun `onStart, should not try to load threads if is not empty`() {
        model.threads.addAll(createEmailThreads(20))

        controller.onStart(null)

        runner.assertPendingWork(listOf(GetMenuInformationWorker::class.java))
    }

    @Test
    fun `after clicking a navigation label, should clear threads list and load new ones`() {
        afterFirstLoad {
            onDrawerMenuEventListenerSlot.captured.onNavigationItemClick(NavigationMenuOptions.TRASH)

            model.selectedLabel `should equal` Label.defaultItems.trash
            model.threads.`should be empty`()

            runner.assertPendingWork(listOf(LoadEmailThreadsWorker::class.java))
        }
    }

    @Test
    fun `pulling down should force mailbox to update`() {
        var didInsertSender = false

        // prepare mocks
        every {
            signal.decryptMessage("mayer", 1, "__ENCRYPTED_TEXT__")
        } returns "__PLAIN_TEXT__"
        every { emailInsertionDao.findContactsByEmail(listOf("mayer@jigl.com")) } returns emptyList()
        every {
            emailInsertionDao.findContactsByEmail(listOf("gabriel@jigl.com"))
            } returns listOf(Contact(id = 0, email ="gabriel@jigl.com", name = "Gabriel Aumala"))
        every {
            emailInsertionDao.insertContacts(listOf(Contact(0, "mayer@jigl.com",
                    "Mayer Mizrachi")))
        } answers { didInsertSender = true; listOf(2) }

        // mock server responses
        val getEventsOneNewEmailResponse = """
            [
          {
            "cmd": 1,
            "params":
                "{\"threadId\":\"gaumala1522191612518\",\"subject\":\"hello\",\"from\":\"Mayer Mizrachi <mayer@jigl.com>\",\"to\":\"gabriel@jigl.com\",\"cc\":\"\",\"bcc\":\"\",\"bodyKey\":\"gaumala1522191612518\",\"preview\":\"\",\"date\":\"2018-03-27 23:00:13\",\"metadataKey\":81}"
          }
        ]"""
        server.enqueue(MockResponse()
                .setBody(getEventsOneNewEmailResponse)
                .setResponseCode(200))
        server.enqueue(MockResponse()
                .setBody("__ENCRYPTED_TEXT__")
                .setResponseCode(200))


        afterFirstLoad {
            observerSlot.captured.onRefreshMails() // trigger pull down to refresh


            runner.assertPendingWork(listOf(UpdateMailboxWorker::class.java))
            runner._work()

            // final assertions
            verify { emailInsertionDao.insertEmail(assert("should have inserted new mail",
                    { e -> e.subject == "hello" && e.content == "__PLAIN_TEXT__"}))
            }
            verify(exactly = 2) { // the update should have triggered the second call
                db.getEmailsFromMailboxLabel(labelTextTypes = MailFolders.INBOX,
                        oldestEmailThread = null,
                        rejectedLabels = any(),
                        limit = 20)
            }
            didInsertSender `should be` true
        }
    }
}