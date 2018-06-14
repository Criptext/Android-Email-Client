package com.email.scenes.settings

import com.email.IHostActivity
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.mocks.MockedWorkRunner
import com.email.scenes.settings.data.SettingsDataSource
import com.email.scenes.settings.data.SettingsRequest
import com.email.scenes.settings.data.SettingsResult
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
    private lateinit var controller: SettingsController
    private lateinit var runner: MockedWorkRunner
    private lateinit var sentRequests: MutableList<SettingsRequest>

    private val observerSlot = CapturingSlot<SettingsUIObserver>()
    private lateinit var listenerSlot: CapturingSlot<(SettingsResult) -> Unit>

    private val newProfileName = "Andres"

    @Before
    fun setUp() {
        runner = MockedWorkRunner()
        scene = mockk(relaxed = true)
        model = SettingsModel()
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"Daniel","jwt":"_JWT_","recipientId":"daniel","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = SettingsController(
                scene = scene,
                model = model,
                host = host,
                activeAccount = activeAccount,
                storage = storage,
                dataSource = dataSource,
                keyboardManager = mockk(relaxed = true))

        listenerSlot = CapturingSlot()

        every {
            scene.attachView("Daniel", model, capture(observerSlot))
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

        listenerSlot.captured(SettingsResult.GetCustomLabels.Success(labels = listOf()))

        val observer = observerSlot.captured
        observer.onProfileNameChanged(newProfileName)

        model.fullName `should equal` newProfileName

        val sentRequest = sentRequests.last()
        sentRequest `should be instance of` SettingsRequest.ChangeContactName::class.java

    }

}