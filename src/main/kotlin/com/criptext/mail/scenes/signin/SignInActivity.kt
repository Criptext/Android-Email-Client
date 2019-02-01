package com.criptext.mail.scenes.signin

import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.signin.data.SignInDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalKeyGenerator
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.CriptextWebSocketFactory

/**
 * Created by sebas on 2/15/18.
 */

class SignInActivity : BaseActivity() {
    override val layoutId: Int = R.layout.activity_sign_in
    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any): SceneController {
        val appCtx = this.applicationContext
        val db: SignInLocalDB.Default = SignInLocalDB.Default(appCtx, appCtx.filesDir)
        val appDB = AppDatabase.getAppDatabase(appCtx)
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB))
        val signInSceneView = SignInScene.SignInSceneView(findViewById(R.id.signin_layout_container))
        val signInSceneModel = receivedModel as SignInSceneModel
        val generalDataSource = GeneralDataSource(
                signalClient = signalClient,
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = KeyValueStorage.SharedPrefs(appCtx),
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = null,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir
        )
        return SignInSceneController(
                webSocketFactory = CriptextWebSocketFactory(),
                model = signInSceneModel,
                scene = signInSceneView,
                host = this,
                storage = KeyValueStorage.SharedPrefs(appCtx),
                generalDataSource = generalDataSource,
                dataSource = SignInDataSource(
                        filesDir = this.filesDir,
                        runner = AsyncTaskWorkRunner(),
                        keyGenerator = SignalKeyGenerator.Default(DeviceUtils.getDeviceType()),
                        httpClient = HttpClient.Default(),
                        signUpDao = appDB.signUpDao(),
                        keyValueStorage = KeyValueStorage.SharedPrefs(appCtx),
                        signInLocalDB = db, accountDao = appDB.accountDao(),
                        db = appDB),
                keyboard = KeyboardManager(this)
        )
    }
}
