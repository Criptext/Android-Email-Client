package com.criptext.mail.scenes.settings.replyto

import android.os.Bundle
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.replyto.data.ReplyToDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.file.AndroidFs
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketSingleton

class ReplyToActivity: BaseActivity(){

    override val layoutId = R.layout.activity_replyto
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val model = receivedModel as ReplyToModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = ReplyToScene.Default(view)
        val db = SettingsLocalDB.Default(appDB)
        val storage = KeyValueStorage.SharedPrefs(this)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val dataSource = ReplyToDataSource(
                settingsLocalDB = db,
                httpClient = HttpClient.Default(),
                activeAccount = activeAccount,
                runner = AsyncTaskWorkRunner(),
                storage = storage)
        val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
        val webSocketEvents = if(jwts.isNotEmpty())
            WebSocketSingleton.getInstance(jwts)
        else
            WebSocketSingleton.getInstance(activeAccount.jwt)
        val generalDataSource = GeneralDataSource(
                signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount)),
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = storage,
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir
        )
        return ReplyToController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                generalDataSource = generalDataSource,
                keyboardManager = KeyboardManager(this),
                storage = storage,
                activeAccount = activeAccount,
                host = this,
                websocketEvents = webSocketEvents)
    }

}