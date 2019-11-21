package com.criptext.mail.scenes.signup

import android.os.Bundle
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.bgworker.RunnableThrottler
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.signup.data.SignUpDataSource
import com.criptext.mail.scenes.signup.data.SignUpAPIClient
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager

/**
 * Created by sebas on 2/16/18.
 */

class SignUpActivity: BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_up

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val signalKeyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType())
        val signUpSceneView = SignUpScene.SignUpSceneView(findViewById(R.id.signup_layout_container))
        val signUpSceneModel = receivedModel as SignUpSceneModel
        val keyValueStorage = KeyValueStorage.SharedPrefs(this)
        val runnableThrottler = RunnableThrottler.Default(500L)
        return SignUpSceneController(
                model = signUpSceneModel,
                keyboardManager = KeyboardManager(this),
                scene = signUpSceneView,
                host = this,
                runnableThrottler = runnableThrottler,
                dataSource = SignUpDataSource(runner = AsyncTaskWorkRunner(),
                        httpClient = HttpClient.Default(),
                        db = appDB,
                        keyValueStorage = keyValueStorage,
                        signalKeyGenerator = signalKeyGenerator))
    }
}
