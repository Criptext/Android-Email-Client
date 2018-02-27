package com.email.scenes.signup

import com.email.mocks.MockedWorkRunner
import com.email.scenes.signin.SignUpDataSource
import com.email.scenes.signin.SignUpSceneController
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
        controller.signUpListener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        controller.signUpListener `should be` null
    }
}
