package com.email.scenes.emailDetail

import com.email.db.DeliveryTypes
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.mocks.MockedWorkRunner
import com.email.mocks.MockedIHostActivity
import com.email.scenes.emailDetail.mocks.MockedEmailDetailLocalDB
import com.email.scenes.emailDetail.mocks.MockedEmailDetailView
import com.email.scenes.emailDetail.mocks.MockedMailboxLocalDB
import com.email.scenes.emailDetail.mocks.MockedSignalProtocolStore
import com.email.scenes.emaildetail.EmailDetailScene
import com.email.scenes.emaildetail.EmailDetailSceneController
import com.email.scenes.emaildetail.EmailDetailSceneModel
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.workers.LoadFullEmailsFromThreadWorker
import com.email.signal.SignalClient
import com.email.utils.DateUtils
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Test
import java.sql.Timestamp

/**
 * Created by sebas on 3/29/18.
 */

open class EmailDetailControllerTest {

    private val mockedThreadId = Timestamp(System.currentTimeMillis()).toString()
    protected lateinit var model: EmailDetailSceneModel
    protected lateinit var scene: EmailDetailScene
    protected lateinit var db: MockedEmailDetailLocalDB
    protected lateinit var mailboxDb: MockedMailboxLocalDB
    protected lateinit var runner: MockedWorkRunner
    protected lateinit var dataSource: EmailDetailDataSource
    protected lateinit var controller: EmailDetailSceneController
    protected lateinit var protocolStore: MockedSignalProtocolStore
    protected lateinit var activeAccount: ActiveAccount
    protected lateinit var emailInsertionDao: EmailInsertionDao
    protected lateinit var sentRequests: MutableList<EmailDetailRequest>

    open fun setUp() {
        protocolStore = MockedSignalProtocolStore()
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"hola","deviceId":1} """)
        model = EmailDetailSceneModel(mockedThreadId, Label.defaultItems.inbox)
        scene = mockk(relaxed = true)
        runner = MockedWorkRunner()
        db = MockedEmailDetailLocalDB()
        mailboxDb = MockedMailboxLocalDB()
        emailInsertionDao = mockk()

        dataSource = mockk(relaxed = true)

        controller = EmailDetailSceneController(
                scene = scene,
                dataSource = dataSource,
                host = MockedIHostActivity(),
                model = model,
                activeAccount = activeAccount,
                keyboard = mockk(relaxed = true))

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs

    }

    private fun createEmailItemsInThread(size: Int): List<FullEmail> {
        return (1..size).map {
            FullEmail(
            email = Email(id = 0,
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
                        delivered = DeliveryTypes.OPENED,
                        messageId = "key",
                        preview = "preview $it" ,
                        secure = true,
                        subject = "Subject $it",
                        threadId = mockedThreadId,
                        unread = false),
                    labels = emptyList(),
                    to = emptyList(),
                    files = emptyList(),
                    cc = emptyList(),
                    bcc = emptyList(),
                    from = Contact(1,"mayer@jigl.com", "Mayer Mizrachi"))
        }.reversed()
    }

}