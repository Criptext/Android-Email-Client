package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import com.email.scenes.LabelChooser.LabelThreadAdapter
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.holders.EmailHolder
import com.email.utils.FlipAnimator
import kotlin.collections.ArrayList

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel, private val dataSource: MailboxDataSource) : SceneController() {


    private val threadListController = ThreadListController(model.threads, scene)

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener{
        override fun onToggleThreadSelection(context: Context, thread: EmailThread, emailHolder: EmailHolder, position: Int) {
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

            // applyIconAnimation(emailHolder, thread, context)
            updateToolbarTitle(multiSelectOn = true)
        }
    }

    fun applyIconAnimation(holder: EmailHolder, mail: EmailThread, mContext: Context) {
        if (mail.isSelected) {
            holder.avatarView.visibility = View.GONE
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE)
            holder.iconBack.setAlpha(1.toFloat())
            FlipAnimator.flipView(mContext,
                    holder.iconBack,
                    holder.avatarView,
                    true);
        } else if(!mail.isSelected){
            holder.iconBack.setVisibility(View.GONE)
            resetIconYAxis(holder.avatarView)
            holder.avatarView.setVisibility(View.VISIBLE);
            FlipAnimator.flipView(mContext, holder.iconBack, holder.avatarView, false);

        }
    }

    private fun resetIconYAxis(view : View) {
        if (view.rotationY != 0.toFloat() ) {
            view.setRotationY(0.toFloat());
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
        var emailThreads = model.selectedThreads.toList()
        dataSource.deleteEmailThreads(emailThreads)

        changeMode(multiSelectON = false, silent = false)
        val fetchEmailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        model.threads = fetchEmailThreads as ArrayList<EmailThread>
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
                activity.startLabelChooserDialog()
                return true
            }
        }

        return true
    }
}
