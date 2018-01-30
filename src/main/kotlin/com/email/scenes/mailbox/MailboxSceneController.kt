package com.email.scenes.mailbox

import android.util.Log
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel, private val dataSource: MailboxDataSource) : SceneController() {

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener{
    }

    override fun onStart() {
        dataSource.seed()
        scene.attachView(threadEventListener)
        model.threads = dataSource.getEmailThreads()
        scene.setEmailList(dataSource.getEmailThreads())
    }

    override fun onStop() {

    }
}
