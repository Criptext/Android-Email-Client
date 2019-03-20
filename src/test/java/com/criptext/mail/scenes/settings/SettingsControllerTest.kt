package com.criptext.mail.scenes.settings

import com.criptext.mail.IHostActivity
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.mocks.MockedWorkRunner
import com.criptext.mail.scenes.settings.data.SettingsDataSource
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.scenes.settings.data.UserSettingsData
import com.criptext.mail.scenes.settings.labels.LabelsController
import com.criptext.mail.scenes.settings.labels.LabelsModel
import com.criptext.mail.scenes.settings.labels.LabelsScene
import com.criptext.mail.scenes.settings.labels.LabelsUIObserver
import com.criptext.mail.scenes.settings.labels.data.LabelsDataSource
import com.criptext.mail.scenes.settings.labels.data.LabelsRequest
import com.criptext.mail.scenes.settings.labels.data.LabelsResult
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketController
import com.criptext.mail.websocket.WebSocketEventPublisher
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

class SettingsControllerTest{

    private lateinit var scene: LabelsScene
    private lateinit var model: LabelsModel
    private lateinit var host: IHostActivity
    private lateinit var activeAccount: ActiveAccount
    private lateinit var storage: KeyValueStorage
    private lateinit var dataSource: LabelsDataSource
    private lateinit var generalDataSource: GeneralDataSource
    private lateinit var controller: LabelsController
    private lateinit var runner: MockedWorkRunner
    protected lateinit var webSocketEvents: WebSocketController
    private lateinit var sentRequests: MutableList<LabelsRequest>

    private val observerSlot = CapturingSlot<LabelsUIObserver>()
    private lateinit var listenerSlot: CapturingSlot<(LabelsResult) -> Unit>

    private val newProfileName = "Andres"

    @Before
    fun setUp() {
        runner = MockedWorkRunner()
        scene = mockk(relaxed = true)
        model = LabelsModel()
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        webSocketEvents = mockk(relaxed = true)
        generalDataSource = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"Daniel","jwt":"_JWT_","recipientId":"daniel","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = LabelsController(
                scene = scene,
                model = model,
                host = host,
                websocketEvents = webSocketEvents,
                activeAccount = activeAccount,
                storage = storage,
                generalDataSource = generalDataSource,
                dataSource = dataSource,
                keyboardManager = mockk(relaxed = true))

        listenerSlot = CapturingSlot()

        every {
            scene.attachView(capture(observerSlot), model)
        } just Runs

        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs
        
    }

    @Test
    fun `on custom label added, should send CreateCustomLabel request`() {

        controller.onStart(null)

        val observer = observerSlot.captured
        observer.onCustomLabelNameAdded("__NEW_CUSTOM_LABEL__")

        val sentRequest = sentRequests.last()
        sentRequest `should be instance of` LabelsRequest.CreateCustomLabel::class.java

    }

}