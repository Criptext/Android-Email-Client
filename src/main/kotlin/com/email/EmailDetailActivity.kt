package com.email

import com.email.bgworker.AsyncTaskWorkRunner
import com.email.bgworker.WorkRunner
import com.email.db.EmailDetailLocalDB
import com.email.db.MailboxLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.EmailDetailScene
import com.email.scenes.emaildetail.EmailDetailSceneController
import com.email.scenes.emaildetail.EmailDetailSceneModel
import com.email.scenes.emaildetail.data.EmailDetailDataSource
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 3/12/18.
 */

class  EmailDetailActivity: BaseActivity() {
    override val layoutId = R.layout.activity_emails_detail
    override val toolbarId = R.id.email_detail_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val mailboxDB: MailboxLocalDB.Default = MailboxLocalDB.
                Default(this.applicationContext)
        val db: EmailDetailLocalDB.Default =
                EmailDetailLocalDB.Default(this.applicationContext)
        val emailDetailModel = receivedModel as EmailDetailSceneModel

        val emailDetailSceneView = EmailDetailScene.EmailDetailSceneView(
                findViewById(R.id.include_emails_detail), this)

        val activeAccount = ActiveAccount.loadFromStorage(this)
        return EmailDetailSceneController(
                model = emailDetailModel,
                scene = emailDetailSceneView,
                host = this,
                mailboxDataSource = MailboxDataSource(
                        activeAccount = activeAccount!!,
                        mailboxLocalDB = mailboxDB,
                        runner = AsyncTaskWorkRunner()),
                dataSource = EmailDetailDataSource(
                        runner = AsyncTaskWorkRunner(),
                emailDetailLocalDB = db )
        )

    }

}
