package com.email.scenes.signup

import android.util.Log
import com.email.api.ApiCall
import com.email.mocks.MockedWorkRunner
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signin.SignUpSceneController
import com.email.scenes.signup.data.RegisterUserWorker
import com.email.scenes.signup.data.SignUpAPIClient
import com.email.scenes.signup.mocks.MockedIHostActivity
import com.email.scenes.signup.mocks.MockedSignUpLocalDB
import com.email.scenes.signup.mocks.MockedSignUpView
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be`
import org.apache.maven.artifact.ant.shaded.StringUtils
import org.junit.Before
import org.junit.Test
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.math.sign

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

        ApiCall.baseUrl = "http://localhost:8000"
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
        initSampleModel(signUpListener)
        scene.errorSignUp = true
        signUpListener.onCreateAccountClick()
        runner.assertPendingWork(listOf(RegisterUserWorker::class.java))
        runner._work()

        scene.errorSignUp `should be` false
    }

    private fun initSampleModel(signUpListener: SignUpSceneController.SignUpListener){
        val id = UUID.randomUUID().toString()
        val messageDigest = MessageDigest.getInstance("MD5")
        messageDigest.update(id.toByteArray())
        val bigUsername = BigInteger(1,messageDigest.digest()).toString(16)
        val username  = StringUtils.abbreviate(bigUsername, 16)
        signUpListener.onUsernameChangedListener(username)
        signUpListener.onCheckedOptionChanged(state = true)
        signUpListener.onFullNameTextChangeListener(text = "Sebastian")
        signUpListener.onPasswordChangedListener(text = "Sebastian")
        signUpListener.onConfirmPasswordChangedListener(text = "Sebastian")
        signUpListener.onRecoveryEmailTextChangeListener(text = "test@mock.com")
    }
}
