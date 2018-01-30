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
        override fun onThreadOpened(id: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onStart() {
        scene.attachView(threadEventListener)
        dataSource.seed()
        model.threads.addAll(dataSource.getEmailThreads())
        scene.setEmailList(dataSource.getEmailThreads())
    }

    override fun onStop() {

    }
}
