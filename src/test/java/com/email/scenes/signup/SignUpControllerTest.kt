package com.email.scenes.signup

import com.email.api.ApiCall
import com.email.mocks.MockedWorkRunner
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.data.RegisterUserWorker
import com.email.scenes.signup.data.SignUpAPIClient
import com.email.scenes.signup.mocks.MockedIHostActivity
import com.email.scenes.signup.mocks.MockedSignUpLocalDB
import com.email.scenes.signup.mocks.MockedSignUpView
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.`should be`
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
        dataSource = SignUpDataSource(
                runner = runner,
                signUpAPIClient = signUpAPIClient,
                signUpLocalDB = db
        )
        controller = SignUpSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  MockedIHostActivity()
        )
    }

    @Test
    fun `onStart should set listeners to the view and data source and onStop should clear them`() {
        controller.onStart()

        dataSource.listener `should not be` null
        scene.signUpListener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        scene.signUpListener `should be` null
    }

    @Test
    fun `when the create user button is clicked, on abscense of error, should update the db and show success of operation`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("Ok")
                .setResponseCode(200))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart()
        val signUpListener = scene.signUpListener
        fillFields(signUpListener)
        fillNewUser(signUpListener)
        scene.errorSignUp = true
        signUpListener?.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        scene.errorSignUp `should be` false
    }

    @Test
    fun `when the create user button is clicked, if there is a duplicated Username, the model should be updated with that error and the scene should show the error`() {
        val server = MockWebServer()

        // Schedule some responses.
        server.enqueue(MockResponse()
                .setBody("Ok")
                .setResponseCode(400))
        ApiCall.baseUrl = server.url("v1/mock").toString()

        controller.onStart()
        val signUpListener = scene.signUpListener
        fillFields(signUpListener)
        fillExistentUser(signUpListener)
        signUpListener?.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        scene.errorSignUp `should be` true
        scene.userNameErrors `should be` true
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
