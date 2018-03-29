package com.email.scenes.emailDetail

import com.email.db.DeliveryTypes
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.FullEmail
import com.email.mocks.MockedWorkRunner
import com.email.scenes.emailDetail.mocks.MockedEmailDetailLocalDB
import com.email.scenes.emailDetail.mocks.MockedEmailDetailView
import com.email.scenes.emailDetail.mocks.MockedMailboxLocalDB
import com.email.scenes.emailDetail.mocks.MockedSignalProtocolStore
import com.email.scenes.emaildetail.EmailDetailSceneController
import com.email.scenes.emaildetail.EmailDetailSceneModel
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.workers.LoadFullEmailsFromThreadWorker
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.signup.mocks.MockedIHostActivity
import com.email.signal.SignalClient
import com.email.utils.DateUtils
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.sql.Timestamp

/**
 * Created by sebas on 3/29/18.
 */

class EmailDetailControllerTest {

    private val mockedThreadId = Timestamp(System.currentTimeMillis()).toString()
    private lateinit var model: EmailDetailSceneModel
    private lateinit var scene: MockedEmailDetailView
    private lateinit var db: MockedEmailDetailLocalDB
    private lateinit var mailboxDb: MockedMailboxLocalDB
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: EmailDetailDataSource
    private lateinit var mailboxDataSource: MailboxDataSource
    private lateinit var controller: EmailDetailSceneController
    private lateinit var protocolStore: MockedSignalProtocolStore
    private lateinit var activeAccount: ActiveAccount

    @Before
    fun setUp() {
        protocolStore = MockedSignalProtocolStore()
        val signalClient = SignalClient.Default(protocolStore)
        activeAccount = ActiveAccount.fromJSONString(""" { "jwt":"John", "recipientId":"hola"} """)
        model = EmailDetailSceneModel(mockedThreadId)
        scene = MockedEmailDetailView()
        runner = MockedWorkRunner()
        db = MockedEmailDetailLocalDB()
        mailboxDb = MockedMailboxLocalDB()

        mailboxDataSource = MailboxDataSource(
                signalClient = signalClient,
                runner = MockedWorkRunner(),
                activeAccount = activeAccount,
                mailboxLocalDB = mailboxDb)

        dataSource = EmailDetailDataSource(
                signalClient = signalClient,
                activeAccount = activeAccount,
                runner = runner,
                emailDetailLocalDB = db)

        controller = EmailDetailSceneController(
                scene = scene,
                mailboxDataSource = mailboxDataSource,
                dataSource = dataSource,
                host = MockedIHostActivity(),
                model = model)

    }

    private fun createEmailItemsInThread(size: Int): List<FullEmail> {
        return (1..size).map {
            FullEmail(
            email = Email(id = null,
                        content = """
                             <!DOCTYPE html>
                                <html>
                                <body>
                                    <h1>My $it Heading</h1>
                                    <p>My $it paragraph.</p>
                                </body>
                            </html>
                        """.trimIndent(),
                        date = DateUtils.getDateFromString(
                                "1992-05-23 20:12:58",
                                null),
                        delivered = DeliveryTypes.RECEIVED,
                        isDraft = false,
                        isTrash = false,
                        key = "key",
                        preview = "preview $it" ,
                        secure = true,
                        subject = "Subject $it",
                        threadid = mockedThreadId,
                        unread = false),
                    labels = emptyList(),
                    to = emptyList(),
                    files = emptyList(),
                    cc = emptyList(),
                    bcc = emptyList(),
                    from = null)
        }.reversed()
    }

    @Test
    fun `onStart should set listeners to the view and data source and onStop should clear them`() {

        controller.onStart()

        dataSource.listener `should not be` null
        mailboxDataSource.listener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        mailboxDataSource.listener `should be` null
    }

    @Test
    fun `on a cold start, should show 'empty view' and load emails from thread`() {

        controller.onStart()

        runner.assertPendingWork(listOf(LoadFullEmailsFromThreadWorker::class.java))
        db.nextLoadedEmailItems = createEmailItemsInThread(5)

        runner._work()

        model.fullEmailList.size  `should be` 5
        scene.notifiedDataSetChanged `should be` true
    }

}