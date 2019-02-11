package com.criptext.mail.scenes.settings.profile

import com.criptext.mail.IHostActivity
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.mocks.MockedWorkRunner
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.scenes.settings.data.UserSettingsData
import com.criptext.mail.scenes.settings.profile.data.ProfileDataSource
import com.criptext.mail.scenes.settings.profile.data.ProfileRequest
import com.criptext.mail.scenes.settings.profile.data.ProfileResult
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.websocket.WebSocketController
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

class ProfileControllerTest{

    private lateinit var scene: ProfileScene
    private lateinit var model: ProfileModel
    private lateinit var host: IHostActivity
    private lateinit var activeAccount: ActiveAccount
    private lateinit var storage: KeyValueStorage
    private lateinit var dataSource: ProfileDataSource
    private lateinit var generalDataSource: GeneralDataSource
    private lateinit var controller: ProfileController
    private lateinit var runner: MockedWorkRunner
    protected lateinit var webSocketEvents: WebSocketController
    private lateinit var sentRequests: MutableList<GeneralRequest>

    private val observerSlot = CapturingSlot<ProfileUIObserver>()
    private lateinit var listenerSlot: CapturingSlot<(ProfileResult) -> Unit>

    private val newProfileName = "Andres"

    @Before
    fun setUp() {
        runner = MockedWorkRunner()
        scene = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        webSocketEvents = mockk(relaxed = true)
        generalDataSource = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"Daniel","jwt":"_JWT_","recipientId":"daniel","deviceId":1
                    |, "signature":""} """.trimMargin())
        model = ProfileModel(activeAccount.name, activeAccount.userEmail, false)
        controller = ProfileController(
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
            scene.attachView(capture(observerSlot), activeAccount.recipientId, model)
        } just Runs

        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs

        sentRequests = mutableListOf()
        every { generalDataSource.submitRequest(capture(sentRequests)) } just Runs
        
    }

    @Test
    fun `on profile name changed, should send ChangeContactName request`() {

        controller.onStart(null)

        val observer = observerSlot.captured
        observer.onProfileNameChanged(newProfileName)

        val sentRequest = sentRequests.last()
        sentRequest `should be instance of` GeneralRequest.ChangeContactName::class.java

    }
}