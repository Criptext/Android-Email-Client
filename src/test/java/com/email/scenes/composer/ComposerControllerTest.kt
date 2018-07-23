package com.email.scenes.composer

import com.email.IHostActivity
import com.email.R
import com.email.bgworker.BackgroundWorkManager
import com.email.db.models.ActiveAccount
import com.email.scenes.composer.data.ComposerRequest
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.composer.data.ComposerType
import com.email.utils.KeyboardManager
import io.mockk.mockk

open class ComposerControllerTest {
    protected lateinit var scene: ComposerScene
    protected lateinit var model: ComposerModel
    protected lateinit var controller: ComposerController
    protected lateinit var dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>
    protected lateinit var host: IHostActivity
    protected lateinit var activeAccount: ActiveAccount

    open fun setUp() {
        model = ComposerModel(ComposerType.Empty())
        model.fileKey = "test_key_16bytes:test_iv_16_bytes"
        scene = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"hola","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = ComposerController(model, scene, host, activeAccount, dataSource)
    }

    protected fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }
}