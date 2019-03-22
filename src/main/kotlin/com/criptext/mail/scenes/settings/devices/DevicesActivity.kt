package com.criptext.mail.scenes.settings.devices

import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.devices.data.DevicesDataSource
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketSingleton

class DevicesActivity: BaseActivity(){

    override val layoutId = R.layout.activity_devices
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as DevicesModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = DevicesScene.Default(view)
        val appDB = AppDatabase.getAppDatabase(this)
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB))
        val activeAccount = ActiveAccount.loadFromStorage(this)
        val webSocketEvents = WebSocketSingleton.getInstance(
                activeAccount = activeAccount!!)

        val dataSource = DevicesDataSource(
                httpClient = HttpClient.Default(),
                activeAccount = activeAccount!!,
                runner = AsyncTaskWorkRunner(),
                storage = KeyValueStorage.SharedPrefs(this),
                accountDao = appDB.accountDao())
        val generalDataSource = GeneralDataSource(
                signalClient = signalClient,
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = KeyValueStorage.SharedPrefs(this),
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir
        )
        return DevicesController(
                model = model,
                scene = scene,
                websocketEvents = webSocketEvents,
                generalDataSource = generalDataSource,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
    }

}