package com.email.scenes.mailbox

import com.email.IHostActivity
import com.email.db.DeliveryTypes
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.dao.signal.RawSessionDao
import com.email.db.models.*
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.data.*
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.signal.SignalClient
import com.email.websocket.WebSocketEventPublisher
import io.mockk.*
import org.amshove.kluent.`should be empty`
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
    private lateinit var api: MailboxAPIClient
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: MailboxDataSource
    private lateinit var controller: MailboxSceneController
    private lateinit var host: IHostActivity
    private lateinit var webSocketEvents: WebSocketEventPublisher
    private lateinit var feedController : FeedController

    private val threadEventListenerSlot = CapturingSlot<EmailThreadAdapter.OnThreadEventListener>()
    private val onDrawerMenuEventListenerSlot = CapturingSlot<DrawerMenuItemListener>()

    @Before
    fun setUp() {
        model = MailboxSceneModel()
        // mock MailboxScene capturing the thread event listener
        scene = mockk(relaxed = true)
        every {
            scene.attachView(MailFolders.INBOX, capture(threadEventListenerSlot),
                    capture(onDrawerMenuEventListenerSlot), any())
        } just Runs

        runner = MockedWorkRunner()
        db = mockk(relaxed = true)
        rawSessionDao = mockk()
        api = MailboxAPIClient("__JWT_TOKEN")
        signal = mockk()

        host = mockk()

        dataSource = MailboxDataSource(
                runner = runner,
                signalClient = signal,
                mailboxLocalDB = db,
                activeAccount = ActiveAccount("gabriel", "__JWT_TOKEN__"),
                rawSessionDao = rawSessionDao
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
}