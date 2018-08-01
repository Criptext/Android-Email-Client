package com.criptext.mail.scenes.signin

import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.signin.data.SignInDataSource
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager

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
                        keyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType(appCtx)),
                        httpClient = HttpClient.Default(),
                        signUpDao = appDB.signUpDao(),
                        keyValueStorage = KeyValueStorage.SharedPrefs(appCtx),
                        signInLocalDB = db, accountDao = appDB.accountDao()),
                keyboard = KeyboardManager(this)
        )
    }
}
