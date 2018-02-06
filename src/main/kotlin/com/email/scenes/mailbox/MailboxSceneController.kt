package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.MenuItem
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.mailbox.data.MailboxDataSource
import kotlin.collections.ArrayList

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel, private val dataSource: MailboxDataSource) : SceneController() {


    private val threadListController = ThreadListController(model.threads, scene)

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener{
        override fun onToggleThreadSelection(context: Context, thread: EmailThread, position: Int) {
            if (! model.isInMultiSelect) {
                changeMode(multiSelectON = true, silent = false)
            }

            val selectedThreads = model.selectedThreads

            if (thread.isSelected) {
                unselectThread(thread, position)
            } else {
                selectThread(thread, position)
            }


            if (selectedThreads.isEmpty()) {
                changeMode(multiSelectON = false, silent = false)
                updateToolbarTitle(multiSelectOn = false)
            }

            updateToolbarTitle(multiSelectOn = true)
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

    fun changeMode(multiSelectON: Boolean, silent: Boolean){
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
        scene.addToolbar()
        val emailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        val labelThreads : List<LabelThread> = dataSource.getAllLabels()
        threadListController.setThreadList(emailThreads as ArrayList<EmailThread>)
        model.threads.addAll(emailThreads)
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
        threadListController.setThreadList(fetchEmailThreads as ArrayList<EmailThread>)
        model.threads.clear()
        model.threads.addAll(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }
    fun deleteSelectedEmailThreads() {
        var emailThreads = model.selectedThreads.toList()
        dataSource.deleteEmailThreads(emailThreads)

        changeMode(multiSelectON = false, silent = false)
        val fetchEmailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads as ArrayList<EmailThread>)
        model.threads.clear()
        model.threads.addAll(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    fun toggleReadSelectedEmailThreads() {
    }

    fun showMultiModeBar() {
        val selectedThreadsQuantity : Int = model.selectedThreads.length()
        scene.showMultiModeBar(selectedThreadsQuantity)
    }

    fun updateToolbarTitle(multiSelectOn :Boolean) {
        if(multiSelectOn) {
            val selectedThreadsQuantity : Int = model.selectedThreads.length()
            scene.updateToolbarTitle(selectedThreadsQuantity.toString())
        } else {
            scene.updateToolbarTitle("INBOX")
        }

    }
    fun hideMultiModeBar() {
        scene.hideMultiModeBar()
    }
    override fun onBackPressed(activity: Activity) {
        scene.onBackPressed(activity)
    }

    fun toggleMultiModeBar() {
        if(model.isInMultiSelect) {
            showMultiModeBar()
        } else {
            hideMultiModeBar()
        }
    }

    fun addTintInMultiMode(context:Context, item: MenuItem) {
        var drawable : Drawable = item.icon
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.multiModeTint));
        item.setIcon(drawable)
    }

    fun onOptionSelected(item: MenuItem?) : Boolean {
        when(item?.itemId) {
            R.id.mailbox_search -> {
                TODO("HANDLE SEARCH CLICK...")
                return true
            }

            R.id.mailbox_bell_container -> {
                TODO("HANDLE BELL CLICK...")
                return true
            }
            R.id.mailbox_archive_selected_messages -> {
                archiveSelectedEmailThreads()
                return true
            }
            R.id.mailbox_delete_selected_messages -> {
                deleteSelectedEmailThreads()
                return true
            }

            R.id.mailbox_toggle_read_selected_messages -> {
                TODO("HANDLE TOGGLE READ SELECTED MESSAGES")
                toggleReadSelectedEmailThreads()
                return true
            }
            R.id.mailbox_move_to -> {
                TODO("Handle move to")
                return true
            }
            R.id.mailbox_add_labels ->{
                val sceneView : MailboxScene.MailboxSceneView =
                        (scene as MailboxScene.MailboxSceneView)
                val activity : MailboxActivity = sceneView.hostActivity as MailboxActivity
                scene.hostActivity.
                showDialogLabelChooser()

                return true
            }
        }

        return true
    }
}
