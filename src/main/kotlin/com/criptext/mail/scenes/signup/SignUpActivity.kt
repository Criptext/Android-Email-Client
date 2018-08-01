package com.criptext.mail.scenes.signup

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

/**
 * Created by sebas on 2/16/18.
 */

class SignUpActivity: BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_up

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val signalKeyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType(this.applicationContext))
        val signUpSceneView = SignUpScene.SignUpSceneView(findViewById(R.id.signup_layout_container))
        val signUpSceneModel = receivedModel as SignUpSceneModel
        val keyValueStorage = KeyValueStorage.SharedPrefs(this)
        val runnableThrottler = RunnableThrottler.Default(500L)
        return SignUpSceneController(
                model = signUpSceneModel,
                scene = signUpSceneView,
                host = this,
                runnableThrottler = runnableThrottler,
                dataSource = SignUpDataSource(runner = AsyncTaskWorkRunner(),
                        httpClient = HttpClient.Default(),
                        db = appDB.signUpDao(),
                        keyValueStorage = keyValueStorage,
                        signalKeyGenerator = signalKeyGenerator))
    }
}
