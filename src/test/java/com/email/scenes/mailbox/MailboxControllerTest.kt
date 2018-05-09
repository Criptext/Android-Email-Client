package com.email.scenes.mailbox

import com.email.IHostActivity
import com.email.bgworker.WorkHandler
import com.email.scenes.mailbox.data.MailboxRequest
import com.email.scenes.mailbox.data.MailboxResult
import com.email.scenes.mailbox.feed.FeedController
import com.email.signal.SignalClient
import com.email.utils.virtuallist.VirtualListView
import com.email.websocket.WebSocketEventPublisher
import io.mockk.*

/**
 * Created by gabriel on 5/9/18.
 */
open class MailboxControllerTest {

    protected lateinit var model: MailboxSceneModel
    protected lateinit var scene: MailboxScene
    protected lateinit var signal: SignalClient
    protected lateinit var dataSource: WorkHandler<MailboxRequest, MailboxResult>
    protected lateinit var controller: MailboxSceneController
    protected lateinit var host: IHostActivity
    protected lateinit var webSocketEvents: WebSocketEventPublisher
    protected lateinit var feedController : FeedController
    protected lateinit var sentRequests: MutableList<MailboxRequest>
    protected lateinit var virtualListView: VirtualListView

    open fun setUp() {
        model = MailboxSceneModel()

        scene = mockk(relaxed = true)
        signal = mockk()
        host = mockk()
        feedController = mockk(relaxed = true)
        webSocketEvents = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)

        virtualListView = mockk(relaxed = true)
        every { scene::virtualListView.get() } returns virtualListView

        controller = MailboxSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  host,
                feedController = feedController,
                websocketEvents = webSocketEvents
        )

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs
    }

}