package com.criptext.mail.scenes.settings.signature

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
import com.criptext.mail.scenes.settings.data.SettingsDataSource
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource

class SignatureActivity: BaseActivity(){

    override val layoutId = R.layout.activity_signature
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val model = receivedModel as SignatureModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = SignatureScene.Default(view)
        val db = SettingsLocalDB.Default(appDB)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val storage = KeyValueStorage.SharedPrefs(this)
        val dataSource = SettingsDataSource(
                settingsLocalDB = db,
                httpClient = HttpClient.Default(),
                activeAccount = activeAccount,
                runner = AsyncTaskWorkRunner(),
                storage = storage)
        return SignatureController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this,
                generalDataSource = GeneralDataSource(
                        storage = storage,
                        httpClient = HttpClient.Default(),
                        db = appDB,
                        activeAccount = activeAccount,
                        eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                        filesDir = this.filesDir,
                        runner = AsyncTaskWorkRunner(),
                        signalClient = null
                ))
    }

}