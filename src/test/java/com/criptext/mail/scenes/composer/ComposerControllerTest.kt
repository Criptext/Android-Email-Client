package com.criptext.mail.scenes.composer

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.composer.data.ComposerRequest
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.remotechange.data.RemoteChangeDataSource
import com.criptext.mail.utils.remotechange.data.RemoteChangeRequest
import com.criptext.mail.utils.remotechange.data.RemoteChangeResult
import io.mockk.mockk

open class ComposerControllerTest {
    protected lateinit var scene: ComposerScene
    protected lateinit var model: ComposerModel
    protected lateinit var controller: ComposerController
    protected lateinit var dataSource: BackgroundWorkManager<ComposerRequest, ComposerResult>
    protected lateinit var remoteChangeDataSource: BackgroundWorkManager<RemoteChangeRequest, RemoteChangeResult>
    protected lateinit var host: IHostActivity
    protected lateinit var activeAccount: ActiveAccount

    open fun setUp() {
        model = ComposerModel(ComposerType.Empty())
        model.fileKey = "test_key_16bytes:test_iv_16_bytes"
        scene = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        remoteChangeDataSource = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"hola","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = ComposerController(model, scene, host, activeAccount, remoteChangeDataSource, dataSource)
    }

    protected fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }
}