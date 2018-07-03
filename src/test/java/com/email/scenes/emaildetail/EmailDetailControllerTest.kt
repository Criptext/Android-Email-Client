package com.email.scenes.emaildetail

import android.Manifest
import com.email.BaseActivity
import com.email.db.DeliveryTypes
import com.email.db.EmailDetailLocalDB
import com.email.db.MailboxLocalDB
import com.email.db.dao.EmailInsertionDao
import com.email.db.models.*
import com.email.email_preview.EmailPreview
import com.email.mocks.MockedWorkRunner
import com.email.mocks.MockedIHostActivity
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.utils.DateUtils
import com.email.websocket.WebSocketEventPublisher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.whispersystems.libsignal.state.SignalProtocolStore
import java.sql.Date
import java.sql.Timestamp

/**
 * Created by sebas on 3/29/18.
 */

open class EmailDetailControllerTest {

    private val mockedThreadId = Timestamp(System.currentTimeMillis()).toString()
    private val mockedEmailPreview = EmailPreview(threadId = mockedThreadId, subject = "",
            count = 0, bodyPreview = "", topText = "", timestamp = Date(System.currentTimeMillis()),
            deliveryStatus = DeliveryTypes.DELIVERED, emailId = 1L, isSelected = false,
            senderName = "", unread = false)

    protected lateinit var model: EmailDetailSceneModel
    protected lateinit var scene: EmailDetailScene
    protected lateinit var host: EmailDetailActivity
    protected lateinit var db: EmailDetailLocalDB
    protected lateinit var mailboxDb: MailboxLocalDB
    protected lateinit var runner: MockedWorkRunner
    protected lateinit var dataSource: EmailDetailDataSource
    protected lateinit var controller: EmailDetailSceneController
    protected lateinit var protocolStore: SignalProtocolStore
    protected lateinit var activeAccount: ActiveAccount
    protected lateinit var emailInsertionDao: EmailInsertionDao
    protected lateinit var sentRequests: MutableList<EmailDetailRequest>
    protected lateinit var websocketEvents: WebSocketEventPublisher

    open fun setUp() {
        protocolStore = mockk()
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"hola","deviceId":1} """)
        model = EmailDetailSceneModel(mockedThreadId, Label.defaultItems.inbox, mockedEmailPreview)
        scene = mockk(relaxed = true)
        runner = MockedWorkRunner()
        db = mockk()
        mailboxDb = mockk()
        emailInsertionDao = mockk()
        host = mockk(relaxed = true)

        every {
            host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal, Manifest.permission.READ_EXTERNAL_STORAGE)
        } returns true
        websocketEvents = mockk(relaxed = true)

        dataSource = mockk(relaxed = true)

        controller = EmailDetailSceneController(
                scene = scene,
                dataSource = dataSource,
                host = host,
                model = model,
                activeAccount = activeAccount,
                keyboard = mockk(relaxed = true),
                websocketEvents = websocketEvents)

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs

    }

    protected fun createEmailItemsInThread(size: Int): List<FullEmail> {
        return (1..size).map {
            FullEmail(
            email = Email(id = it.toLong(),
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
                        delivered = DeliveryTypes.READ,
                        messageId = "key",
                        preview = "bodyPreview $it" ,
                        secure = true,
                        subject = "Subject $it",
                        threadId = mockedThreadId,
                        metadataKey = it + 100L,
                        unread = false),
                    labels = emptyList(),
                    to = emptyList(),
                    files = arrayListOf(CRFile(token = "efhgfdgdfsg$it",
                            name = "test.pdf",
                            size = 65346L,
                            status = 1,
                            date = DateUtils.getDateFromString(
                                    "1992-05-23 20:12:58",
                                    null),
                            readOnly = false,
                            emailId = it.toLong()
                            )),
                    cc = emptyList(),
                    bcc = emptyList(),
                    from = Contact(1,"mayer@jigl.com", "Mayer Mizrachi"))
        }.reversed()
    }

}