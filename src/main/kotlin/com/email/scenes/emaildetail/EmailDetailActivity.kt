package com.email.scenes.emaildetail

import com.email.BaseActivity
import com.email.R
import com.email.api.HttpClient
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.EmailDetailLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.utils.KeyboardManager

/**
 * Created by sebas on 3/12/18.
 */

class  EmailDetailActivity: BaseActivity() {

    override val layoutId = R.layout.activity_emails_detail
    override val toolbarId = R.id.email_detail_toolbar

    override fun initController(receivedModel: Any): SceneController {

        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val db: EmailDetailLocalDB.Default =
                EmailDetailLocalDB.Default(this.applicationContext)
        val emailDetailModel = receivedModel as EmailDetailSceneModel
        val httpClient = HttpClient.Default()

        val emailDetailSceneView = EmailDetailScene.EmailDetailSceneView(
                findViewById(R.id.include_emails_detail), this)

        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        return EmailDetailSceneController(
                model = emailDetailModel,
                scene = emailDetailSceneView,
                host = this,
                activeAccount = activeAccount,
                dataSource = EmailDetailDataSource(
                        runner = AsyncTaskWorkRunner(),
                        emailDao = appDB.emailDao(),
                        httpClient = httpClient,
                        activeAccount = activeAccount,
                        emailDetailLocalDB = db),
                keyboard = KeyboardManager(this)
        )

    }

}
