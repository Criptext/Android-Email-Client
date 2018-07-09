package com.email.scenes.settings

import android.view.ViewGroup
import com.email.BaseActivity
import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.db.SettingsLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.settings.data.SettingsDataSource
import com.email.utils.KeyboardManager

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