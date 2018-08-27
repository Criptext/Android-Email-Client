package com.criptext.mail.scenes.settings.change_email

import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.change_email.data.ChangeEmailDataSource
import com.criptext.mail.utils.KeyboardManager

class ChangeEmailActivity: BaseActivity(){

    override val layoutId = R.layout.activity_change_email
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as ChangeEmailModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = ChangeEmailScene.Default(view)
        val dataSource = ChangeEmailDataSource(
                httpClient = HttpClient.Default(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                runner = AsyncTaskWorkRunner(),
                storage = KeyValueStorage.SharedPrefs(this))
        return ChangeEmailController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
    }

}