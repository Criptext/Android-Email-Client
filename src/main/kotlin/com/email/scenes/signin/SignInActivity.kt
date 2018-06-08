package com.email.scenes.signin

import com.email.BaseActivity
import com.email.R
import com.email.api.HttpClient
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.db.SignInLocalDB
import com.email.scenes.SceneController
import com.email.scenes.signin.data.SignInDataSource
import com.email.signal.SignalKeyGenerator
import com.email.utils.KeyboardManager

/**
 * Created by sebas on 2/15/18.
 */

class SignInActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_in
    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val appCtx = this.applicationContext
        val db: SignInLocalDB.Default = SignInLocalDB.Default(appCtx)
        val appDB = AppDatabase.getAppDatabase(appCtx)
        val signInSceneView = SignInScene.SignInSceneView(findViewById(R.id.signin_layout_container))
        val signInSceneModel = receivedModel as SignInSceneModel
        return SignInSceneController(
                model = signInSceneModel,
                scene = signInSceneView,
                host = this,
                dataSource = SignInDataSource(
                        runner = AsyncTaskWorkRunner(),
                        keyGenerator = SignalKeyGenerator.Default(),
                        httpClient = HttpClient.Default(),
                        signUpDao = appDB.signUpDao(),
                        keyValueStorage = KeyValueStorage.SharedPrefs(appCtx),
                        signInLocalDB = db),
                keyboard = KeyboardManager(this)
        )
    }
}
