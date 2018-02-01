package com.email.scenes.mailbox

import android.provider.ContactsContract
import com.email.DB.seeders.EmailLabelSeeder
import com.email.androidui.mailthread.ThreadListController
import android.app.Activity
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel, private val dataSource: MailboxDataSource) : SceneController() {


    private val threadListController = ThreadListController(model.threads, scene)

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener{
        override fun onToggleThreadSelection(thread: EmailThread, position: Int) {
            if (! model.isInMultiSelect) {
                changeMode(multiSelectON = true, silent = false)
            }

            val selectedThreads = model.selectedThreads
            if (thread.isSelected) {
                unselectThread(thread, position)
            } else {
                selectThread(thread, position)
            }

            if (selectedThreads.isEmpty())
                changeMode(multiSelectON = false, silent = false)
        }
    }

    private fun selectThread(thread: EmailThread, position: Int) {
        model.selectedThreads.add(thread)
        scene.notifyThreadChanged(position)
    }

    private fun unselectThread(thread: EmailThread, position: Int) {
        model.selectedThreads.remove(thread)
        scene.notifyThreadChanged(position)
    }

    private fun changeMode(multiSelectON: Boolean, silent: Boolean){
        if(! multiSelectON){
            model.selectedThreads.clear()
        }
        model.isInMultiSelect = multiSelectON
        scene.changeMode(multiSelectON, silent)
        scene.refreshToolbarItems()
    }

    override fun onStart() {
        scene.attachView(threadEventListener)
        scene.initDrawerLayout()
        scene.initNavHeader()
        dataSource.seed()
        scene.attachView(threadEventListener)
        val emailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(emailThreads)
        model.threads = emailThreads as ArrayList<EmailThread>
    }

    override fun onStop() {

    }

    fun archiveSelectedEmailThreads() {
        var emailThreads = model.selectedThreads.toList()
        (emailThreads).forEach {
            dataSource.removeLabelsRelation(it.labelsOfMail, it.id)
        }

        changeMode(multiSelectON = false, silent = false)
        val fetchEmailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        model.threads = fetchEmailThreads as ArrayList<EmailThread>
        scene.notifyThreadSetChanged()
    }
    fun deleteSelectedEmailThreads() {

    }

    fun toggleReadSelectedEmailThreads() {

    }


    override fun onBackPressed(activity: Activity) {
        scene.onBackPressed(activity)
    }
}
