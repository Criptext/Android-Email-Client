package com.email.scenes.signup

import com.email.api.ApiCall
import com.email.db.KeyValueStorage
import com.email.mocks.MockedHostActivity
import com.email.mocks.MockedKeyValueStorage
import com.email.mocks.MockedSignalKeyGenerator
import com.email.mocks.MockedWorkRunner
import com.email.scenes.signup.data.SignUpDataSource
import com.email.scenes.signup.data.RegisterUserWorker
import com.email.scenes.signup.data.SignUpAPIClient
import com.email.scenes.signup.mocks.MockedSignUpLocalDB
import com.email.scenes.signup.mocks.MockedSignUpView
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Test

/**
 * Created by sebas on 2/27/18.
 */

class SignUpControllerTest {

    private lateinit var model: SignUpSceneModel
    private lateinit var scene: MockedSignUpView
    private lateinit var db: MockedSignUpLocalDB
    private lateinit var storage: KeyValueStorage
    private lateinit var signUpAPIClient: SignUpAPIClient
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: SignUpDataSource
    private lateinit var controller: SignUpSceneController

    @Before
    fun setUp() {
        model = SignUpSceneModel()
        scene = MockedSignUpView()
        runner = MockedWorkRunner()
        db = MockedSignUpLocalDB()
        signUpAPIClient = SignUpAPIClient.Default()
        storage = MockedKeyValueStorage()
        dataSource = SignUpDataSource(
                runner = runner,
                signUpAPIClient = signUpAPIClient,
                signUpLocalDB = db,
                signalKeyGenerator = MockedSignalKeyGenerator(),
                keyValueStorage = storage
        )
        controller = SignUpSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  MockedHostActivity()
        )
    }

    @Test
    fun `onStart should set listeners to the view and data source and onStop should clear them`() {
        controller.onStart(null)

        dataSource.listener `should not be` null
        scene.signUpListener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        scene.signUpListener `should be` null
    }

    @Test
    fun `when the create user button is clicked, on absence of error, should update the db and local storage and show success in UI`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("Ok")
                .setResponseCode(200))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart(null)

        // simulate user input
        val signUpListener = scene.signUpListener
        fillFields(signUpListener)
        fillNewUser(signUpListener)
        scene.errorSignUp = true
        signUpListener?.onCreateAccountClick()

        // trigger work complete
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        // assert UI has no errors
        scene.errorSignUp `should be` false
        // assert db and local storage updated
        storage.getString(KeyValueStorage.StringKey.ActiveAccount, "") `should equal` """{"jwt":"Ok","recipientId":"sebas1"}"""
        db.savedUser!!.recipientId `should equal` "sebas1"
    }

    @Test
    fun `when the create user button is clicked, if there is a duplicated Username, the model should be updated with that error and the scene should show the error`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("Ok")
                .setResponseCode(400))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart(null)
        val signUpListener = scene.signUpListener
        fillFields(signUpListener)
        fillExistentUser(signUpListener)
        signUpListener?.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        // assert UI has errors
        scene.errorSignUp `should be` true
        scene.userNameErrors `should be` true
        // assert db and local storage did not updated
        storage.getString(KeyValueStorage.StringKey.ActiveAccount, "") `should equal` ""
        db.savedUser `should be` null
    }

    private fun fillNewUser(signUpListener: SignUpSceneController.SignUpListener?){
        signUpListener?.onUsernameChangedListener("sebas1")
    }

    private fun fillExistentUser(signUpListener: SignUpSceneController.SignUpListener?){
        signUpListener?.onUsernameChangedListener("sebas")
    }

    private fun fillFields(signUpListener: SignUpSceneController.SignUpListener?) {
        if(signUpListener != null) {
            signUpListener.onCheckedOptionChanged(state = true)
            signUpListener.onFullNameTextChangeListener(text = "Sebastian")
            signUpListener.onPasswordChangedListener(text = "Sebastian")
            signUpListener.onConfirmPasswordChangedListener(text = "Sebastian")
            signUpListener.onRecoveryEmailTextChangeListener(text = "test@mock.com")
        }
    }
}
