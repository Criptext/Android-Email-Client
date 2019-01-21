package com.criptext.mail.scenes.settings.replyto

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
import com.criptext.mail.scenes.settings.replyto.data.ReplyToDataSource
import com.criptext.mail.utils.KeyboardManager

class ReplyToActivity: BaseActivity(){

    override val layoutId = R.layout.activity_replyto
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as ReplyToModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = ReplyToScene.Default(view)
        val db = SettingsLocalDB.Default(appDB)
        val dataSource = ReplyToDataSource(
                settingsLocalDB = db,
                httpClient = HttpClient.Default(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                runner = AsyncTaskWorkRunner(),
                storage = KeyValueStorage.SharedPrefs(this))
        return ReplyToController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
    }

}