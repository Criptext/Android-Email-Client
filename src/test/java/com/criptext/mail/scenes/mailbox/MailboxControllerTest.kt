package com.criptext.mail.scenes.mailbox

import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.mocks.MockedIHostActivity
import com.criptext.mail.mocks.MockedKeyValueStorage
import com.criptext.mail.scenes.mailbox.data.MailboxDataSource
import com.criptext.mail.scenes.mailbox.data.MailboxRequest
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.scenes.mailbox.feed.FeedController
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.websocket.WebSocketEventPublisher
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk

/**
 * Created by gabriel on 5/9/18.
 */
open class MailboxControllerTest {

    protected lateinit var model: MailboxSceneModel
    protected lateinit var scene: MailboxScene
    protected lateinit var storage: MockedKeyValueStorage
    protected lateinit var signal: SignalClient
    protected lateinit var dataSource: MailboxDataSource
    protected lateinit var generalDataSource: GeneralDataSource
    protected lateinit var controller: MailboxSceneController
    protected lateinit var host: MockedIHostActivity
    protected lateinit var webSocketEvents: WebSocketEventPublisher
    protected lateinit var feedController : FeedController
    protected lateinit var sentRequests: MutableList<MailboxRequest>
    protected lateinit var sentGeneralRequests: MutableList<GeneralRequest>
    protected lateinit var virtualListView: VirtualListView
    protected lateinit var activeAccount: ActiveAccount

    open fun setUp() {
        model = MailboxSceneModel()

        scene = mockk(relaxed = true)
        signal = mockk()
        host = MockedIHostActivity()
        storage = MockedKeyValueStorage()
        feedController = mockk(relaxed = true)
        webSocketEvents = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        generalDataSource = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"gabriel","deviceId":1,
                    |"signature":""} """.trimMargin())

        virtualListView = mockk(relaxed = true)
        every { scene::virtualListView.get() } returns virtualListView

        controller = MailboxSceneController(
                model =  model,
                scene = scene,
                generalDataSource = generalDataSource,
                dataSource = dataSource,
                host =  host,
                storage = storage,
                feedController = feedController,
                activeAccount = activeAccount,
                websocketEvents = webSocketEvents
        )

        sentRequests = mutableListOf()
        sentGeneralRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs
        every { generalDataSource.submitRequest(capture(sentGeneralRequests)) } just Runs
    }

}