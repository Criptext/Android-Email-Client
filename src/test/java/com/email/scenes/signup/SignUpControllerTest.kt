package com.email.scenes.signup

import com.email.mocks.MockedWorkRunner
import com.email.mocks.api.MockSignUpAPIClient
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.data.RegisterUserWorker
import com.email.scenes.signup.data.SignUpAPIClient
import com.email.scenes.signup.mocks.MockedIHostActivity
import com.email.scenes.signup.mocks.MockedSignUpLocalDB
import com.email.scenes.signup.mocks.MockedSignUpView
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
        signUpAPIClient = MockSignUpAPIClient(listOf("andres", "sebas"))
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
        controller.signUpListener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        controller.signUpListener `should be` null
    }

    @Test
    fun `when the create user button is clicked, on abscense of error, should update the db and show success of operation`() {
        controller.onStart()
        val signUpListener = controller.signUpListener!!
        fillFields(signUpListener)
        fillNewUser(signUpListener)
        scene.errorSignUp = true
        signUpListener.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        scene.errorSignUp `should be` false
    }

    @Test
    fun `when the create user button is clicked, if there is a duplicated Username, the model should be updated with that error and the scene should show the error`() {
        controller.onStart()
        val signUpListener = controller.signUpListener!!
        fillFields(signUpListener)
        fillExistentUser(signUpListener)
        signUpListener.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        scene.errorSignUp `should be` true
        scene.userNameErrors `should be` true
    }

    private fun fillNewUser(signUpListener: SignUpSceneController.SignUpListener){
        signUpListener.onUsernameChangedListener("sebas1")
    }

    private fun fillExistentUser(signUpListener: SignUpSceneController.SignUpListener){
        signUpListener.onUsernameChangedListener("sebas")
    }

    private fun fillFields(signUpListener: SignUpSceneController.SignUpListener) {
        signUpListener.onCheckedOptionChanged(state = true)
        signUpListener.onFullNameTextChangeListener(text = "Sebastian")
        signUpListener.onPasswordChangedListener(text = "Sebastian")
        signUpListener.onConfirmPasswordChangedListener(text = "Sebastian")
        signUpListener.onRecoveryEmailTextChangeListener(text = "test@mock.com")
    }
}
