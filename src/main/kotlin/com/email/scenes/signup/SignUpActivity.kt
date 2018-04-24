package com.email.scenes.signup

import com.email.BaseActivity
import com.email.R
import com.email.signal.SignalKeyGenerator
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.scenes.SceneController
import com.email.scenes.signup.data.SignUpDataSource
import com.email.scenes.signup.data.SignUpAPIClient

/**
 * Created by sebas on 2/16/18.
 */

class SignUpActivity: BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_up

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val signalKeyGenerator = SignalKeyGenerator.Default()
        val signUpSceneView = SignUpScene.SignUpSceneView(findViewById(R.id.signup_layout_container))
        val signUpSceneModel = receivedModel as SignUpSceneModel
        val keyValueStorage = KeyValueStorage.SharedPrefs(this)
        return SignUpSceneController(
                model = signUpSceneModel,
                scene = signUpSceneView,
                host = this,
                dataSource = SignUpDataSource(runner = AsyncTaskWorkRunner(),
                        signUpAPIClient = SignUpAPIClient.Default(),
                        db = appDB.signUpDao(),
                        keyValueStorage = keyValueStorage,
                        signalKeyGenerator = signalKeyGenerator))
    }
}
