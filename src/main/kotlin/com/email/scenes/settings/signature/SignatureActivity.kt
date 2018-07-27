package com.email.scenes.settings.signature

import android.view.ViewGroup
import com.email.BaseActivity
import com.email.R
import com.email.api.HttpClient
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.db.SettingsLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.settings.data.SettingsDataSource
import com.email.utils.KeyboardManager

class SignatureActivity: BaseActivity(){

    override val layoutId = R.layout.activity_signature
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as SignatureModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = SignatureScene.Default(view)
        val db = SettingsLocalDB(appDB.labelDao(), appDB.accountDao(), appDB.contactDao())
        val dataSource = SettingsDataSource(
                settingsLocalDB = db,
                httpClient = HttpClient.Default(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                runner = AsyncTaskWorkRunner())
        return SignatureController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
    }

}