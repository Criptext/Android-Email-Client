package com.email.scenes.signin

import com.email.IHostActivity
import com.email.bgworker.WorkRunner
import com.email.db.SignInLocalDB
import com.email.scenes.params.SignUpParams
import com.email.scenes.signin.data.SignInAPIClient
import com.email.scenes.signin.data.SignInDataSource
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 4/20/18.
 */
class SignInControllerTest {

    private lateinit var model: SignInSceneModel
    private lateinit var scene: SignInScene
    private lateinit var runner: WorkRunner
    private lateinit var apiClient: SignInAPIClient
    private lateinit var localDB: SignInLocalDB
    private lateinit var dataSource: SignInDataSource
    private lateinit var host: IHostActivity
    private lateinit var controller: SignInSceneController
    private val signInUIObserverSlot = CapturingSlot<SignInSceneController.SignInUIObserver>()

    @Before
    fun setUp() {
        model = SignInSceneModel()

        // mock SignInScene capturing the UI Observer
        scene = mockk<SignInScene>(relaxed = true)
        every { scene.initListeners(capture(signInUIObserverSlot)) } just Runs

        runner = mockk<WorkRunner>()
        apiClient = mockk<SignInAPIClient>()
        localDB = mockk<SignInLocalDB>()
        host = mockk<IHostActivity>(relaxed = true)
        dataSource = SignInDataSource(runner, signInAPIClient = apiClient, signInLocalDB = localDB)
        controller = SignInSceneController(model, scene, host, dataSource)
    }

    @After
    fun tearDown() {
        signInUIObserverSlot.clear()
    }

    @Test
    fun `should go to sign up scene when sign up label is clicked`() {
        controller.onStart(null)

        signInUIObserverSlot.captured.onSignUpLabelClicked()

        verify { host.goToScene(SignUpParams(), false) }
    }
}
