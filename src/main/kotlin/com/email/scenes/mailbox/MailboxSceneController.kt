package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import android.app.Activity
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel, private val dataSource: MailboxDataSource) : SceneController() {


    private val threadListController = ThreadListController(model.threads as ArrayList<EmailThread>, scene)

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener{
        override fun onToggleThreadSelection(thread: EmailThread, position: Int) {
            if (! model.isInMultiSelect) {
                changeMode(multiSelectON = true, silent = false)
            }

            val selectedThreads = model.selectedThreads
            if (thread.isSelected) {
                selectedThreads.remove(thread)
                scene.notifyThreadChanged(position)
            } else {
                selectedThreads.add(thread)
                scene.notifyThreadChanged(position)
            }

            if (selectedThreads.isEmpty())
                changeMode(multiSelectON = false, silent = false)
        }
    }

    private fun changeMode(multiSelectON: Boolean, silent: Boolean){
        if(! multiSelectON){
            model.selectedThreads.clear()
        }
        model.isInMultiSelect = multiSelectON
        scene.changeMode(multiSelectON, silent)
    }

    override fun onStart() {
        scene.attachView(threadEventListener)
        scene.initDrawerLayout()
        scene.initNavHeader()
        dataSource.seed()
        scene.attachView(threadEventListener)
        val emailThreads : List<EmailThread> = dataSource.getEmailThreads()
        threadListController.populateThreads(emailThreads)
        model.threads = emailThreads as ArrayList<EmailThread>
        scene.setEmailList(emailThreads)
    }

    override fun onStop() {

    }

    override fun onBackPressed(activity: Activity) {
        scene.onBackPressed(activity)
    }
}
