package com.email.scenes.composer

import com.email.IHostActivity
import com.email.R
import com.email.bgworker.BackgroundWorkManager
import com.email.scenes.composer.data.ComposerRequest
import com.email.scenes.composer.data.ComposerResult
import io.mockk.mockk

open class ComposerControllerTest {
    protected lateinit var scene: ComposerScene
    protected lateinit var model: ComposerModel
    protected lateinit var controller: ComposerController
    protected lateinit var dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>
    protected lateinit var host: IHostActivity

    open fun setUp() {
        model = ComposerModel(fullEmail = null, composerType = null)
        scene = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)

        controller = ComposerController(model, scene, host, dataSource)
    }

    protected fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }
}