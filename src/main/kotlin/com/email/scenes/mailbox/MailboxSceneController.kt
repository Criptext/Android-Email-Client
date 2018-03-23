package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import android.content.Context
import com.email.IHostActivity
import com.email.R
import com.email.scenes.labelChooser.LabelDataHandler
import com.email.scenes.labelChooser.SelectedLabels
import com.email.scenes.labelChooser.data.LabelWrapper
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.*
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.ui.MailboxUIObserver
import com.email.scenes.params.ComposerParams
import com.email.scenes.params.EmailDetailParams
import com.email.scenes.params.SearchParams

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val host: IHostActivity,
                             private val dataSource: MailboxDataSource,
                             private val feedController : FeedController) : SceneController() {

    private val dataSourceListener = { result: MailboxResult ->
        when (result) {
            is MailboxResult.GetLabels -> onLabelsLoaded(result)
            is MailboxResult.UpdateMailbox -> dataSourceController.onMailboxUpdated(result)
        }
    }

    private fun onLabelsLoaded(result: MailboxResult.GetLabels) {
        when (result) {
            is MailboxResult.GetLabels.Success -> {
                scene.onFetchedLabels(result.defaultSelectedLabels,
                        result.labels)
            }

            is MailboxResult.GetLabels.Failure -> {

            }
        }
    }

    private val toolbarTitle: String
        get() = if (model.isInMultiSelect) model.selectedThreads.length().toString()
        else "INBOX"

    override val menuResourceId: Int?
        get() = when {
            !model.isInMultiSelect -> R.menu.mailbox_menu_normal_mode
            model.hasSelectedUnreadMessages -> R.menu.mailbox_menu_multi_mode_unread
            else -> R.menu.mailbox_menu_multi_mode_read
        }
    private val emailThreadSize: Int
        get() = model.threads.size

    private val threadListController = ThreadListController(model.threads, scene)
    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener {
        override fun onGoToMail(emailThread: EmailThread) {
            host.goToScene(EmailDetailParams(emailThread.threadId))
        }

        override fun onToggleThreadSelection(context: Context, thread: EmailThread, position: Int) {
            if (!model.isInMultiSelect) {
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
    private val dataSourceController = DataSourceController(dataSource)
    private val observer = object : MailboxUIObserver {
        override fun onRefreshMails() {
            dataSourceController.updateMailbox(
                    mailboxLabel = model.label,
                    isManual = true)
        }

        override fun onOpenComposerButtonClicked() {
            host.goToScene(ComposerParams())
        }
    }

    val menuClickListener = { ->
        scene.openNotificationFeed()
    }

    private fun selectThread(thread: EmailThread, position: Int) {
        model.selectedThreads.add(thread)
        scene.notifyThreadChanged(position)
    }

    private fun unselectThread(thread: EmailThread, position: Int) {
        model.selectedThreads.remove(thread)
        scene.notifyThreadChanged(position)
    }

    fun changeMode(multiSelectON: Boolean, silent: Boolean) {

        if (!multiSelectON) {
            model.selectedThreads.clear()
        }
        model.isInMultiSelect = multiSelectON
        scene.changeMode(multiSelectON, silent)
        scene.refreshToolbarItems()
        toggleMultiModeBar()
        scene.updateToolbarTitle(toolbarTitle)
    }

    override fun onStart() {
        dataSourceController.setDataSourceListener()
        scene.attachView(threadEventListener)
        scene.observer = observer
        scene.initDrawerLayout()
        scene.initNavHeader("Daniel Tigse Palma")

        val emailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(emailThreads)
        toggleMultiModeBar()
        scene.setToolbarNumberOfEmails(emailThreadSize)

        feedController.onStart()
    }

    override fun onStop() {
        feedController.onStop()
    }

    private fun archiveSelectedEmailThreads() {
        val emailThreads = model.selectedThreads.toList()
        emailThreads.forEach {
            dataSource.removeLabelsRelation(it.labelsOfMail, it.id)
        }

        changeMode(multiSelectON = false, silent = false)
        val fetchEmailThreads: List<EmailThread> = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.setToolbarNumberOfEmails(emailThreadSize)
        scene.notifyThreadSetChanged()
    }

    private fun deleteSelectedEmailThreads() {
        val emailThreads = model.selectedThreads.toList()
        dataSource.deleteEmailThreads(emailThreads)

        changeMode(multiSelectON = false, silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.setToolbarNumberOfEmails(emailThreadSize)
        scene.notifyThreadSetChanged()
    }

    private fun toggleReadSelectedEmailThreads(unreadStatus: Boolean) {
        val emailThreads = model.selectedThreads.toList()
        dataSource.updateUnreadStatus(emailThreads = emailThreads,
                updateUnreadStatus = !unreadStatus)
        changeMode(multiSelectON = false,
                silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    private fun showMultiModeBar() {
        val selectedThreadsQuantity: Int = model.selectedThreads.length()
        scene.showMultiModeBar(selectedThreadsQuantity)
    }

    private fun hideMultiModeBar() {
        scene.hideMultiModeBar()
    }

    override fun onBackPressed(): Boolean {
        return scene.onBackPressed()
    }

    private fun toggleMultiModeBar() {
        if (model.isInMultiSelect) {
            showMultiModeBar()
        } else {
            hideMultiModeBar()
        }
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.mailbox_search -> host.goToScene(SearchParams())
            R.id.mailbox_archive_selected_messages -> archiveSelectedEmailThreads()
            R.id.mailbox_delete_selected_messages -> deleteSelectedEmailThreads()
            R.id.mailbox_message_toggle_read -> {
                val unreadStatus = model.isInUnreadMode
                toggleReadSelectedEmailThreads(unreadStatus = unreadStatus)
            }
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener)
            }
            R.id.mailbox_add_labels -> {
                showDialogLabelChooser()
            }
        }
    }

    private fun showDialogLabelChooser() {
        val threadIds = model.selectedThreads.toList().map {
            it.threadId
        }
        val req = MailboxRequest.GetLabels(
                threadIds = threadIds
        )

        dataSource.submitRequest(req)
        scene.showDialogLabelsChooser(LabelDataHandler(this))
    }

    fun getAllLabels(): List<LabelWrapper> {
        return dataSource.getAllLabels()
    }

    fun assignLabelToEmailThread(labelId: Int, emailThreadId: Int) {
        dataSource.createLabelEmailRelation(labelId = labelId,
                emailId = emailThreadId)
    }

    fun createRelationSelectedEmailLabels(selectedLabels: SelectedLabels): Boolean {
        model.selectedThreads.toList().forEach {
            val emailThread: EmailThread = it
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

    fun moveSelectedEmailsToSpam() {
        dataSource.moveSelectedEmailThreadsToSpam(model.selectedThreads.toList())
        changeMode(multiSelectON = false, silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    fun moveSelectedEmailsToTrash() {
        dataSource.moveSelectedEmailThreadsToTrash(model.selectedThreads.toList())
        changeMode(multiSelectON = false, silent = false)

        val fetchEmailThreads = dataSource.getNotArchivedEmailThreads()
        threadListController.setThreadList(fetchEmailThreads)
        scene.notifyThreadSetChanged()
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {
        override fun moveToSpam() {
            moveSelectedEmailsToSpam()
        }

        override fun moveToTrash() {
            moveSelectedEmailsToTrash()
        }

    }

    private inner class DataSourceController(
            private val dataSource: MailboxDataSource){
        fun setDataSourceListener() {
            dataSource.listener = dataSourceListener
        }

        fun clearDataSourceListener() {
            dataSource.listener = null
        }

        fun updateMailbox(
                mailboxLabel: String,
                isManual: Boolean): Boolean {
            if(MailboxData.updateMailboxWorkData== null) {
                scene.showSyncingDialog()
                MailboxData.updateMailboxWorkData =
                        MailboxData.UpdateMailboxWorkData()

                val req = MailboxRequest.UpdateMailbox(
                        label = mailboxLabel
                )

                dataSource.submitRequest(req)
                return true
            }
            return false
        }

        private fun handleSuccessfulMailboxUpdate(resultData: MailboxResult.UpdateMailbox.Success) {
            threadListController.populateThreads(resultData.mailboxThreads)
            scene.hideSyncingDialog()
/*            if (resultData.isManual)
                TODO("WHAT TO DO IF IT WAS MANUAL(?)")
            else
                TODO("HIDE SYNCING DIALOG")*/

        }

        private fun handleFailedMailboxUpdate(resultData: MailboxResult.UpdateMailbox.Failure) {
            scene.hideSyncingDialog()
            scene.showError(resultData.message)
        }

        fun onMailboxUpdated(resultData: MailboxResult.UpdateMailbox) {
            MailboxData.updateMailboxWorkData = null
            when (resultData) {
                is MailboxResult.UpdateMailbox.Success ->
                    handleSuccessfulMailboxUpdate(resultData)
                is MailboxResult.UpdateMailbox.Failure ->
                    handleFailedMailboxUpdate(resultData)
            }
/*            if (resultData.getDestinationMailbox() == model.label) {
                model.shouldShowPartialUpdateInUI = false
                scene.clearRefreshing()
                when (resultData) {
                    is MailboxResult.UpdateMailbox.Success ->
                        handleSuccessfulMailboxUpdate(resultData)
                    is MailboxResult.UpdateMailbox.Failure ->
                        handleFailedMailboxUpdate(resultData)
                }
            }*/
        }

    }
}
