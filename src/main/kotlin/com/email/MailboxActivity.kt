package com.email

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.transition.Scene
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory
import com.email.scenes.SceneController
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : AppCompatActivity(), IHostActivity {
    override val activity: Activity by lazy {
        this
    }

    private lateinit var sceneFactory : SceneFactory

    private lateinit var mailboxSceneController: MailboxSceneController
    private var mailboxSceneModel : MailboxSceneModel = MailboxSceneModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)

        sceneFactory = SceneFactory.SceneInflater(this)
        initController()
    }

    override fun initController() {
        mailboxSceneController = MailboxSceneController(
                    scene = sceneFactory.createMailboxScene(),
                    model = mailboxSceneModel, dataSource = MailboxDataSource(MailboxLocalDB.Default(this.applicationContext)))
    }

    override fun onStart() {
        super.onStart()
        mailboxSceneController.onStart()
    }
}