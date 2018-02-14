package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import android.content.Context
import android.view.Menu
import com.email.R
import com.email.scenes.LabelChooser.LabelDataSourceHandler
import com.email.scenes.LabelChooser.SelectedLabels
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val dataSource: MailboxDataSource) : SceneController() {


    private val toolbarTitle: String
        get() = if (model.isInMultiSelect) model.selectedThreads.length().toString()
        else "INBOX"

    override val menuResourceId: Int
        get() = when {
            ! model.isInMultiSelect -> R.menu.mailbox_menu_normal_mode
            model.hasSelectedUnreadMessages -> R.menu.mailbox_menu_multi_mode_unread
            else -> R.menu.mailbox_menu_multi_mode_read
        }

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
            }

            scene.updateToolbarTitle(toolbarTitle)
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
        scene.updateToolbarTitle(toolbarTitle)
    }

    override fun onStart() {
        scene.attachView(threadEventListener)
        scene.initDrawerLayout()
        scene.initNavHeader()
        dataSource.seed()
        scene.attachView(threadEventListener)
        val emailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(emailThreads)
    }

    override fun onStop() {

    }

    fun archiveSelectedEmailThreads() {
        val emailThreads = model.selectedThreads.toList()
        emailThreads.forEach {
            dataSource.removeLabelsRelation(it.labelsOfMail, it.id)
        }

        changeMode(multiSelectON = false, silent = false)
        val fetchEmailThreads : List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }
    fun deleteSelectedEmailThreads() {
        val emailThreads = model.selectedThreads.toList()
        dataSource.deleteEmailThreads(emailThreads)

        changeMode(multiSelectON = false, silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    fun toggleReadSelectedEmailThreads(title: String) {
        val unreadStatus = (title != "read")
        val emailThreads = model.selectedThreads.toList()
            dataSource.updateUnreadStatus(emailThreads = emailThreads,
                    updateUnreadStatus = unreadStatus)
        changeMode(multiSelectON = false,
                silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    fun showMultiModeBar() {
        val selectedThreadsQuantity : Int = model.selectedThreads.length()
        scene.showMultiModeBar(selectedThreadsQuantity)
    }

    fun hideMultiModeBar() {
        scene.hideMultiModeBar()
    }
    override fun onBackPressed(): Boolean {
        return true
    }

    fun toggleMultiModeBar() {
        if(model.isInMultiSelect) {
            showMultiModeBar()
        } else {
            hideMultiModeBar()
        }
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId) {
            R.id.mailbox_search -> {
                TODO("HANDLE SEARCH CLICK...")
            }

            R.id.mailbox_bell_container -> {
                TODO("HANDLE BELL CLICK...")
            }
            R.id.mailbox_archive_selected_messages -> {
                archiveSelectedEmailThreads()
            }
            R.id.mailbox_delete_selected_messages -> {
                deleteSelectedEmailThreads()
            }
            R.id.mailbox_message_toggle_read -> {
                toggleReadSelectedEmailThreads("READ")
            }
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener = OnMoveThreadsListener(this))
            }
            R.id.mailbox_add_labels ->{
                scene.showDialogLabelsChooser(labelDataSourceHandler = LabelDataSourceHandler(this))
            }
        }
    }

    fun getAllLabels() : List<LabelThread>{
        return dataSource.getAllLabels()
    }

    fun assignLabelToEmailThread(labelId: Int, emailThreadId: Int){
        dataSource.createLabelEmailRelation(labelId = labelId,
                emailId = emailThreadId)
    }

    fun createRelationSelectedEmailLabels(selectedLabels : SelectedLabels): Boolean{
        model.selectedThreads.toList().forEach {
            val emailThread : EmailThread = it
            selectedLabels.toIDs().forEach {
                try {
                    assignLabelToEmailThread(it, emailThreadId = emailThread.id)
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                }
            }
        }
        changeMode(multiSelectON = false, silent = false)
        return false
    }

    fun postMenuDisplay(menu: Menu) {
        toggleMultiModeBar()
    }

    fun moveSelectedEmailsToSpam(){
        dataSource.moveSelectedEmailThreadsToSpam(model.selectedThreads.toList())
        changeMode(multiSelectON = false, silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    fun moveSelectedEmailsToTrash(){
        dataSource.moveSelectedEmailThreadsToTrash(model.selectedThreads.toList())
        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }
}
