package com.email.scenes.emaildetail

import com.email.BaseActivity
import com.email.R
import com.email.api.Hosts
import com.email.api.HttpClient
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.EmailDetailLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.utils.KeyboardManager
import com.email.utils.file.AndroidFs

/**
 * Created by sebas on 3/12/18.
 */

class  EmailDetailActivity: BaseActivity() {

    override val layoutId = R.layout.activity_emails_detail
    override val toolbarId = R.id.email_detail_toolbar

    override fun initController(receivedModel: Any): SceneController {

        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val filesHttpClient = HttpClient.Default(Hosts.fileServiceUrl, HttpClient.AuthScheme.basic, 14000L, 7000L)
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
                        emailDetailLocalDB = db,
                        filesHttpClient: filesHttpClient,
                        fileServiceAuthToken = Hosts.fileServiceAuthToken),
                        downloadDir = AndroidFs.getDownloadsCacheDir(this).absolutePath),
                keyboard = KeyboardManager(this)
        )

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller.requestPermissionResult(requestCode, permissions, grantResults)
    }

}
