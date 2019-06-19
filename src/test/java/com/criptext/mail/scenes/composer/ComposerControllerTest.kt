package com.criptext.mail.scenes.composer

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.mocks.MockedKeyValueStorage
import com.criptext.mail.scenes.composer.data.ComposerDataSource
import com.criptext.mail.scenes.composer.data.ComposerRequest
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import io.mockk.mockk

open class ComposerControllerTest {
    protected lateinit var scene: ComposerScene
    protected lateinit var model: ComposerModel
    protected lateinit var controller: ComposerController
    protected lateinit var dataSource: ComposerDataSource
    protected lateinit var generalDataSource: GeneralDataSource
    protected lateinit var host: IHostActivity
    protected lateinit var storage: MockedKeyValueStorage
    protected lateinit var activeAccount: ActiveAccount

    open fun setUp() {
        model = ComposerModel(ComposerType.Empty())
        model.fileKey = "test_key_16bytes:test_iv_16_bytes"
        scene = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        generalDataSource = mockk(relaxed = true)
        storage = MockedKeyValueStorage()
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"hola","deviceId":1
                    |, "signature":""} """.trimMargin())
        controller = ComposerController(storage, model, scene, host, activeAccount, generalDataSource, dataSource)
    }

    protected fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }
}