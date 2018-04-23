package com.email.scenes.signup

import com.email.IHostActivity
import com.email.api.ApiCall
import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao
import com.email.db.models.Account
import com.email.db.models.Label
import com.email.mocks.MockedSignalKeyGenerator
import com.email.mocks.MockedWorkRunner
import com.email.scenes.params.SignInParams
import com.email.scenes.signup.data.SignUpDataSource
import com.email.scenes.signup.data.RegisterUserWorker
import com.email.scenes.signup.data.SignUpAPIClient
import io.mockk.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
import org.junit.Before
import org.junit.Test

/**
 * Created by sebas on 2/27/18.
 */

class SignUpControllerTest {

    private lateinit var model: SignUpSceneModel
    private lateinit var scene: SignUpScene
    private lateinit var db: SignUpDao
    private lateinit var storage: KeyValueStorage
    private lateinit var signUpAPIClient: SignUpAPIClient
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: SignUpDataSource
    private lateinit var controller: SignUpSceneController
    private lateinit var host: IHostActivity
    private val uiObserverSlot = CapturingSlot<SignUpSceneController.SignUpUIObserver>()

    @Before
    fun setUp() {
        model = SignUpSceneModel()

        // mock SignInScene capturing the UI Observer
        scene = mockk<SignUpScene>(relaxed = true)
        every { scene.initListeners(capture(uiObserverSlot)) } just Runs

        runner = MockedWorkRunner()
        db = mockk<SignUpDao>(relaxed = true)
        signUpAPIClient = SignUpAPIClient.Default()
        storage = mockk<KeyValueStorage>(relaxed = true)

        host = mockk<IHostActivity>()
        every { host.exitToScene(any(), null) } just Runs

        dataSource = SignUpDataSource(
                runner = runner,
                signUpAPIClient = signUpAPIClient,
                db = db,
                signalKeyGenerator = MockedSignalKeyGenerator(),
                keyValueStorage = storage
        )
        controller = SignUpSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  host
        )
    }

    @Test
    fun `when the create user button is clicked, on absence of error, should update the db and local storage and show success in UI`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("__JWT_TOKEN__")
                .setResponseCode(200))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart(null)

        // simulate user input
        val uiObserver = uiObserverSlot.captured
        fillFields(uiObserver)
        fillNewUser(uiObserver)
        // fire event
        uiObserver.onCreateAccountClick()

        // assert ui changed after clicking
        verify { scene.showKeyGenerationHolder() }

        // trigger work complete
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        val extraStepsSlot = CapturingSlot<Runnable>()
        // assert UI has no errors
        verify(inverse = true) { scene.showError(any()) }

        // assert data got saved to local storage
        verify {
            val accountMatcher: Account = match {
                it.name == "Sebastian" && it.recipientId == "sebas1"
            }
            val defaultLabelsMatcher: List<Label> = eq(Label.defaultItems.toList())

            db.insertNewAccountData(account = accountMatcher, preKeyList = any(),
                    signedPreKey = any(), defaultLabels = defaultLabelsMatcher,
                    extraRegistrationSteps = capture(extraStepsSlot))
        }

        // run extra steps and assert that they execute the correct effect
        extraStepsSlot.captured.run()
        verify {
            storage.putString(KeyValueStorage.StringKey.ActiveAccount,
                    """{"jwt":"__JWT_TOKEN__","recipientId":"sebas1"}""")
        }
    }

    @Test
    fun `when user presses back, should go to sign in scene`() {
        controller.onStart(null)

        val backEventWasHandledBySystem = controller.onBackPressed()
        backEventWasHandledBySystem `should be` false

        verify { host.exitToScene(SignInParams(), null) }
    }

    @Test
    fun `when the create user button is clicked, if there is a duplicated Username, the model should be updated with that error and the scene should show the error`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("User already exists!")
                .setResponseCode(400))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart(null)

        val uiObserver = uiObserverSlot.captured
        fillFields(uiObserver)
        fillExistentUser(uiObserver)
        // fire event
        uiObserver.onCreateAccountClick()

        // assert ui changed after clicking
        verify { scene.showKeyGenerationHolder() }

        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        // assert UI has errors
        verify { scene.showError(any()) }
        // assert storage was not touched
        verify(inverse = true) { storage.putString(any(), any()) }
        verify(inverse = true) { db.insertNewAccountData(any(), any(), any(), any(), any()) }

    }

    private fun fillNewUser(uiObserver: SignUpSceneController.SignUpUIObserver?){
        uiObserver?.onUsernameChangedListener("sebas1")
    }

    private fun fillExistentUser(uiObserver: SignUpSceneController.SignUpUIObserver?){
        uiObserver?.onUsernameChangedListener("sebas")
    }

    private fun fillFields(uiObserver: SignUpSceneController.SignUpUIObserver?) {
        if(uiObserver != null) {
            uiObserver.onCheckedOptionChanged(state = true)
            uiObserver.onFullNameTextChangeListener(text = "Sebastian")
            uiObserver.onPasswordChangedListener(text = "Sebastian")
            uiObserver.onConfirmPasswordChangedListener(text = "Sebastian")
            uiObserver.onRecoveryEmailTextChangeListener(text = "test@mock.com")
        }
    }
}
