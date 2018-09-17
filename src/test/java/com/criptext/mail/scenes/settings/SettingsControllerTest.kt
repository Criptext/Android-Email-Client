package com.criptext.mail.scenes.settings

import com.criptext.mail.IHostActivity
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.mocks.MockedWorkRunner
import com.criptext.mail.scenes.settings.data.SettingsDataSource
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.scenes.settings.data.UserSettingsData
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketController
import com.criptext.mail.websocket.WebSocketEventPublisher
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

class SettingsControllerTest{

    private lateinit var scene: SettingsScene
    private lateinit var model: SettingsModel
    private lateinit var host: IHostActivity
    private lateinit var activeAccount: ActiveAccount
    private lateinit var storage: KeyValueStorage
    private lateinit var dataSource: SettingsDataSource
    private lateinit var generalDataSource: GeneralDataSource
    private lateinit var controller: SettingsController
    private lateinit var runner: MockedWorkRunner
    protected lateinit var webSocketEvents: WebSocketController
    private lateinit var sentRequests: MutableList<SettingsRequest>

    private val observerSlot = CapturingSlot<SettingsUIObserver>()
    private val itemListenerSlot = CapturingSlot<DevicesListItemListener>()
    private lateinit var listenerSlot: CapturingSlot<(SettingsResult) -> Unit>

    private val newProfileName = "Andres"

    @Before
    fun setUp() {
        runner = MockedWorkRunner()
        scene = mockk(relaxed = true)
        model = SettingsModel()
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        webSocketEvents = mockk(relaxed = true)
        generalDataSource = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"Daniel","jwt":"_JWT_","recipientId":"daniel","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = SettingsController(
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
            scene.attachView("Daniel", model, capture(observerSlot), capture(itemListenerSlot))
        } just Runs

        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs
        
    }

    @Test
    fun `on profile name changed, should send ChangeContactName request`() {

        controller.onStart(null)

        listenerSlot.captured(SettingsResult.GetUserSettings.Success(userSettings = UserSettingsData(listOf(), "", false)))

        val observer = observerSlot.captured
        observer.onProfileNameChanged(newProfileName)

        model.fullName `should equal` newProfileName

        val sentRequest = sentRequests.last()
        sentRequest `should be instance of` SettingsRequest.ChangeContactName::class.java

    }

    @Test
    fun `on custom label added, should send CreateCustomLabel request`() {

        controller.onStart(null)

        listenerSlot.captured(SettingsResult.GetUserSettings.Success(userSettings = UserSettingsData(listOf(), "", false)))

        val observer = observerSlot.captured
        observer.onCustomLabelNameAdded("__NEW_CUSTOM_LABEL__")

        val sentRequest = sentRequests.last()
        sentRequest `should be instance of` SettingsRequest.CreateCustomLabel::class.java

    }

}