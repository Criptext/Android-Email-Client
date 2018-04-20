package com.email.scenes.mailbox

import com.email.androidui.mailthread.ThreadListController
import android.content.Context
import android.util.Log
import com.email.IHostActivity
import com.email.R
import com.email.db.LabelTextTypes
import com.email.db.models.FullEmail
import com.email.db.typeConverters.LabelTextConverter
import com.email.scenes.ActivityMessage
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
import com.email.scenes.params.MailboxParams
import com.email.scenes.params.SearchParams
import com.email.websocket.WebSocketEventListener
import com.email.websocket.WebSocketEventPublisher

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val host: IHostActivity,
                             private val dataSource: MailboxDataSource,
                             private val websocketEvents: WebSocketEventPublisher,
                             private val feedController : FeedController) : SceneController() {

    private val dataSourceListener = { result: MailboxResult ->
        when (result) {
            is MailboxResult.GetLabels -> onLabelsLoaded(result)
            is MailboxResult.UpdateMailbox -> dataSourceController.onMailboxUpdated(result)
            is MailboxResult.LoadEmailThreads -> dataSourceController.onLoadedMoreThreads(result)
            is MailboxResult.SendMail -> onSendMailFinished(result)
            is MailboxResult.UpdateEmailThreadsLabelsRelations -> dataSourceController.onUpdatedLabels(result)
            is MailboxResult.UpdateMail -> host.finishScene()
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
    private fun getTitleForMailbox() : String{
        return LabelTextConverter().parseLabelTextType(model.label)
    }

    private fun onSendMailFinished(result: MailboxResult.SendMail) {
        when (result) {
            is MailboxResult.SendMail.Success -> {
                dataSource.submitRequest(MailboxRequest.UpdateEmail(result.emailId, result.response))
            }
            is MailboxResult.SendMail.Failure -> scene.showError(result.message)
        }
    }

    private val toolbarTitle: String
        get() = if (model.isInMultiSelect) model.selectedThreads.length().toString()
        else getTitleForMailbox()

    override val menuResourceId: Int?
        get() = when {
            !model.isInMultiSelect -> R.menu.mailbox_menu_normal_mode
            model.hasSelectedUnreadMessages -> R.menu.mailbox_menu_multi_mode_unread
            else -> R.menu.mailbox_menu_multi_mode_read
        }
    private val emailThreadSize: Int
        get() = model.threads.size

    private val onScrollListener = object: OnScrollListener {
        override fun onReachEnd() {
                model.loadingType = LoadingType.APPEND
                val req = MailboxRequest.LoadEmailThreads(
                        label = model.label,
                        offset = model.offset,
                        oldestEmailThread = model.oldestEmailThread)
                dataSource.submitRequest(req)
        }
    }

    private fun loadMailbox(labelTextType: LabelTextTypes, lastEmailThread: EmailThread?) {

        model.label = labelTextType
        val req = MailboxRequest.LoadEmailThreads(
                label = model.label,
                offset = model.offset,
                oldestEmailThread = lastEmailThread)
        dataSource.submitRequest(req)
    }

    private val threadListController = ThreadListController(model.threads, scene)
    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener {
        override fun onGoToMail(emailThread: EmailThread) {
            host.goToScene(EmailDetailParams(emailThread.threadId), true)
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
    private val onDrawerMenuItemListener = object: DrawerMenuItemListener {
        override fun onNavigationItemClick(navigationMenuOptions: NavigationMenuOptions) {
            scene.hideDrawer()
            scene.showRefresh()

            when(navigationMenuOptions) {
                NavigationMenuOptions.INBOX -> model.label = LabelTextTypes.INBOX
                NavigationMenuOptions.SENT -> model.label = LabelTextTypes.SENT
                NavigationMenuOptions.DRAFT -> model.label = LabelTextTypes.DRAFT
                NavigationMenuOptions.STARRED -> model.label = LabelTextTypes.STARRED
                NavigationMenuOptions.SPAM -> model.label = LabelTextTypes.SPAM
                NavigationMenuOptions.TRASH -> model.label = LabelTextTypes.TRASH
            }

            loadMailbox(
                    labelTextType = model.label,
                    lastEmailThread = null)
        }
    }

    private val dataSourceController = DataSourceController(dataSource)
    private val observer = object : MailboxUIObserver {
        override fun onRefreshMails() {
            scene.showRefresh()
            dataSourceController.updateMailbox(
                    mailboxLabel = model.label,
                    isManual = true)
        }

        override fun onOpenComposerButtonClicked() {
            host.goToScene(ComposerParams(), true)
        }
    }

    val menuClickListener = {
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

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        return when (activityMessage) {
            null -> false
            is ActivityMessage.SendMail -> {
                val newRequest = MailboxRequest.SendMail(activityMessage.emailId,
                        activityMessage.composerInputData)
                dataSource.submitRequest(newRequest)
                true
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSourceController.setDataSourceListener()
        scene.attachView(
                mailboxLabel = model.label,
                threadEventListener = threadEventListener,
                onDrawerMenuItemListener = onDrawerMenuItemListener,
                onScrollListener = onScrollListener)
        scene.observer = observer
        scene.initDrawerLayout()
        scene.initNavHeader("Daniel Tigse Palma")

        loadMailbox(
                labelTextType = model.label,
                lastEmailThread = null)

        toggleMultiModeBar()
        scene.setToolbarNumberOfEmails(emailThreadSize)
        feedController.onStart()

        websocketEvents.subscribe(this.javaClass, webSocketEventListener)

        return handleActivityMessage(activityMessage)
    }

    override fun onStop() {

        websocketEvents.unsubscribe(this.javaClass)
        feedController.onStop()
    }

    private fun archiveSelectedEmailThreads() {
        dataSourceController.updateEmailThreadsLabelsRelations(
                selectedLabels = null,
                chosenLabel = LabelTextTypes.ARCHIVED)
        }

    private fun deleteSelectedEmailThreads() {
        dataSourceController.updateEmailThreadsLabelsRelations(
                selectedLabels = null,
                chosenLabel = LabelTextTypes.TRASH)
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
        scene.updateToolbarTitle(toolbarTitle)
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
            R.id.mailbox_search -> host.goToScene(SearchParams(), true)
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

    fun createRelationSelectedEmailLabels(selectedLabels: SelectedLabels): Boolean {
        dataSourceController.updateEmailThreadsLabelsRelations(
                selectedLabels,
                null)

        return false
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {
        override fun moveToSpam() {
            dataSourceController.updateEmailThreadsLabelsRelations(
                    selectedLabels = null,
                    chosenLabel = LabelTextTypes.SPAM)
        }

        override fun moveToTrash() {
            dataSourceController.updateEmailThreadsLabelsRelations(
                    selectedLabels = null,
                    chosenLabel = LabelTextTypes.TRASH)
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

        fun updateEmailThreadsLabelsRelations(
                selectedLabels: SelectedLabels?,
                chosenLabel: LabelTextTypes?
        ): Boolean {
                val req = MailboxRequest.UpdateEmailThreadsLabelsRelations(
                        selectedEmailThreads = model.selectedThreads.toList(),
                        selectedLabels = selectedLabels,
                        chosenLabel = chosenLabel)

                dataSource.submitRequest(req)
                return true
            }

        fun updateMailbox(
                mailboxLabel: LabelTextTypes,
                isManual: Boolean): Boolean {
            scene.hideDrawer()
            if(MailboxData.updateMailboxWorkData== null) {
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
            scene.setToolbarNumberOfEmails(emailThreadSize)
            scene.updateToolbarTitle(toolbarTitle)
        }

        private fun handleFailedMailboxUpdate(resultData: MailboxResult.UpdateMailbox.Failure) {
            scene.showError(resultData.message)
        }

        fun onMailboxUpdated(resultData: MailboxResult.UpdateMailbox) {
            MailboxData.updateMailboxWorkData = null
            scene.clearRefreshing()
            when (resultData) {
                is MailboxResult.UpdateMailbox.Success ->
                    handleSuccessfulMailboxUpdate(resultData)
                is MailboxResult.UpdateMailbox.Failure ->
                    handleFailedMailboxUpdate(resultData)
            }
        }

        fun onLoadedMoreThreads(result: MailboxResult.LoadEmailThreads) {
            scene.clearRefreshing()
            scene.updateToolbarTitle(toolbarTitle)
            MailboxData.loadThreadsWorkData = null
            when(result) {
                is MailboxResult.LoadEmailThreads.Success -> {
                    if(model.loadingType == LoadingType.FULL) {
                        threadListController.populateThreads(result.emailThreads)
                        scene.setToolbarNumberOfEmails(emailThreadSize)
                        scene.notifyThreadSetChanged()
                       return
                    }else {
                        threadListController.appendThreads(result.emailThreads)
                        scene.setToolbarNumberOfEmails(emailThreadSize)
                        scene.notifyThreadSetChanged()
                        model.loadingType = LoadingType.FULL
                    }
                }
            }
        }

        fun onUpdatedLabels(result: MailboxResult.UpdateEmailThreadsLabelsRelations) {
            changeMode(multiSelectON = false, silent = false)
            when(result) {
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Success ->  {
                    loadMailbox(
                            labelTextType = model.label,
                            lastEmailThread = null)
                } else -> {

                }
            }
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onMailOpened(token: String, message: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onFileOpenedOrDownloaded(mailToken: String, message: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onNewMessage(emailThread: EmailThread) {
            model.threads.add(0, emailThread)
            scene.setToolbarNumberOfEmails(emailThreadSize)
            scene.notifyThreadSetChanged()
            scene.scrollTop()
        }

        override fun onUnsent(token: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onMuteMessage() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onUserStatusChange(status: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


    }

}
