package com.criptext.mail.scenes.settings

import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsDataSource
import com.criptext.mail.utils.KeyboardManager

class SettingsActivity: BaseActivity(){

    override val layoutId = R.layout.activity_settings
    override val toolbarId = null

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as SettingsModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = SettingsScene.Default(view)
        val db = SettingsLocalDB(appDB.labelDao(), appDB.accountDao(), appDB.contactDao())
        val dataSource = SettingsDataSource(
                settingsLocalDB = db,
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                httpClient = HttpClient.Default(),
                runner = AsyncTaskWorkRunner())
        return SettingsController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                storage = KeyValueStorage.SharedPrefs(this),
                host = this)
    }

}