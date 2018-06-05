package com.email.scenes.signup

import com.email.IHostActivity
import com.email.bgworker.RunnableThrottler
import com.email.bgworker.BackgroundWorkManager
import com.email.scenes.signup.data.SignUpRequest
import com.email.scenes.signup.data.SignUpResult
import io.mockk.*

/**
 * Created by gabriel on 5/16/18.
 */
open class SignUpControllerTest {
    protected lateinit var model: SignUpSceneModel
    protected lateinit var scene: SignUpScene
    protected lateinit var dataSource: BackgroundWorkManager<SignUpRequest, SignUpResult>
    protected lateinit var sentRequests: MutableList<SignUpRequest>
    protected lateinit var controller: SignUpSceneController
    protected lateinit var host: IHostActivity
    protected lateinit var throttler: RunnableThrottler

    open fun setUp() {
        model = SignUpSceneModel()

        // mock SignInScene capturing the UI Observer
        scene = mockk(relaxed = true)

        host = mockk(relaxed = true)
        throttler = mockk()

        sentRequests = mutableListOf()
        dataSource = mockk(relaxed = true)
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs

        controller = SignUpSceneController(
                model =  model,
                scene = scene,
                dataSource = dataSource,
                host =  host,
                runnableThrottler = throttler
        )
    }

}