package com.email.scenes.mailbox

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.email.IHostActivity
import com.email.R
import com.email.api.models.TrackingUpdate
import com.email.bgworker.BackgroundWorkManager
import com.email.db.MailFolders
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.db.typeConverters.LabelTextConverter
import com.email.email_preview.EmailPreview
import com.email.scenes.ActivityMessage
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerType
import com.email.scenes.mailbox.data.*
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.ui.MailboxUIObserver
import com.email.scenes.params.ComposerParams
import com.email.scenes.params.EmailDetailParams
import com.email.scenes.params.SearchParams
import com.email.scenes.params.SettingsParams
import com.email.utils.UIMessage
import com.email.websocket.WebSocketEventListener
import com.email.websocket.WebSocketEventPublisher

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val host: IHostActivity,
                             private val dataSource: BackgroundWorkManager<MailboxRequest, MailboxResult>,
                             private val activeAccount: ActiveAccount,
                             private val websocketEvents: WebSocketEventPublisher,
                             private val feedController : FeedController) : SceneController() {

    private val threadListController = ThreadListController(model, scene.virtualListView)
    private val dataSourceListener = { result: MailboxResult ->
        when (result) {
            is MailboxResult.GetSelectedLabels -> dataSourceController.onSelectedLabelsLoaded(result)
            is MailboxResult.UpdateMailbox -> dataSourceController.onMailboxUpdated(result)
            is MailboxResult.LoadEmailThreads -> dataSourceController.onLoadedMoreThreads(result)
            is MailboxResult.SendMail -> dataSourceController.onSendMailFinished(result)
            is MailboxResult.UpdateEmailThreadsLabelsRelations -> dataSourceController.onUpdatedLabels(result)
            is MailboxResult.MoveEmailThread -> dataSourceController.onMoveEmailThread(result)
            is MailboxResult.GetMenuInformation -> dataSourceController.onGetMenuInformation(result)
            is MailboxResult.UpdateUnreadStatus -> dataSourceController.onUpdateUnreadStatus(result)
        }
    }

    private fun getTitleForMailbox() : String{
        return LabelTextConverter().parseLabelTextType(model.selectedLabel.text)
    }

    private val toolbarTitle: String
        get() = if (model.isInMultiSelect) model.selectedThreads.length().toString()
        else getTitleForMailbox()

    override val menuResourceId: Int?
        get() = when {
            !model.isInMultiSelect -> R.menu.mailbox_menu_normal_mode
            model.hasSelectedUnreadMessages -> {
                when {
                    model.selectedLabel == Label.defaultItems.draft -> R.menu.mailbox_menu_multi_mode_unread_draft
                    model.selectedLabel == Label.defaultItems.spam -> R.menu.mailbox_menu_multi_mode_unread_spam
                    model.selectedLabel == Label.defaultItems.trash -> R.menu.mailbox_menu_multi_mode_unread_trash
                    model.selectedLabel.id < 0 -> R.menu.mailbox_menu_multi_mode_unread_allmail
                    else -> R.menu.mailbox_menu_multi_mode_unread
                }
            }
            else -> {
                when {
                    model.selectedLabel == Label.defaultItems.draft -> R.menu.mailbox_menu_multi_mode_read_draft
                    model.selectedLabel == Label.defaultItems.spam -> R.menu.mailbox_menu_multi_mode_read_spam
                    model.selectedLabel == Label.defaultItems.trash -> R.menu.mailbox_menu_multi_mode_read_trash
                    model.selectedLabel.id < 0 -> R.menu.mailbox_menu_multi_mode_read_allmail
                    else -> R.menu.mailbox_menu_multi_mode_read
                }
            }
        }

    private fun getTotalUnreadThreads(): Int{
        return model.threads.fold(0, { total, next ->
            total + (if(next.unread) 1 else 0)
        })
    }

    private fun reloadMailboxThreads() {
        val req = MailboxRequest.LoadEmailThreads(
                label = model.selectedLabel.text,
                loadParams = LoadParams.Reset(size = threadsPerPage),
                userEmail = activeAccount.userEmail)
        dataSource.submitRequest(req)
    }

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener {
        override fun onApproachingEnd() {
                val req = MailboxRequest.LoadEmailThreads(
                        label = model.selectedLabel.text,
                        loadParams = LoadParams.NewPage(size = threadsPerPage,
                        startDate = model.threads.lastOrNull()?.timestamp),
                        userEmail = activeAccount.userEmail)
                dataSource.submitRequest(req)
        }

        override fun onGoToMail(emailPreview: EmailPreview) {

            if(emailPreview.count == 1 &&
                    model.selectedLabel == Label.defaultItems.draft){
                val params = ComposerParams(ComposerType.Draft(draftId = emailPreview.emailId,
                        threadPreview = emailPreview, currentLabel = model.selectedLabel))
                return host.goToScene(params, true)
            }
            host.goToScene(EmailDetailParams(threadId = emailPreview.threadId,
                    currentLabel = model.selectedLabel, threadPreview = emailPreview), true)
        }

        override fun onToggleThreadSelection(thread: EmailPreview, position: Int) {
            if (!model.isInMultiSelect) {
                changeMode(multiSelectON = true, silent = false)
            }

            val selectedThreads = model.selectedThreads

            if (thread.isSelected) {
                threadListController.unselect(thread, position)
            } else {
                threadListController.select(thread, position)
            }

            if (selectedThreads.isEmpty()) {
                changeMode(multiSelectON = false, silent = false)
            }

            scene.updateToolbarTitle(toolbarTitle)
        }
    }
    private val onDrawerMenuItemListener = object: DrawerMenuItemListener {

        override fun onSettingsOptionClicked() {
            host.goToScene(SettingsParams(), true)
        }

        override fun onNavigationItemClick(navigationMenuOptions: NavigationMenuOptions) {
            scene.hideDrawer()

            when(navigationMenuOptions) {
                NavigationMenuOptions.INBOX,
                NavigationMenuOptions.SENT,
                NavigationMenuOptions.DRAFT,
                NavigationMenuOptions.STARRED,
                NavigationMenuOptions.SPAM,
                NavigationMenuOptions.TRASH,
                NavigationMenuOptions.ALL_MAIL -> {
                    scene.showRefresh()
                    model.selectedLabel = navigationMenuOptions.toLabel()!!
                    reloadMailboxThreads()
                }
                else -> { /* do nothing */ }
            }

        }
    }

    private val dataSourceController = DataSourceController(dataSource)
    private val observer = object : MailboxUIObserver {

        override fun onFeedDrawerClosed() {
            feedController.lastTimeFeedOpened = System.currentTimeMillis()
            feedController.reloadFeeds()
        }

        override fun onBackButtonPressed() {
            if(model.isInMultiSelect){
                changeMode(multiSelectON = false, silent = false)
                threadListController.reRenderAll()
            }
        }

        override fun onRefreshMails() {
            scene.showRefresh()
            dataSourceController.updateMailbox(
                    mailboxLabel = model.selectedLabel)
        }

        override fun onOpenComposerButtonClicked() {
            val params = ComposerParams(ComposerType.Empty())
            host.goToScene(params, true)
        }
    }

    val menuClickListener = {
        scene.openNotificationFeed()
    }


    fun changeMode(multiSelectON: Boolean, silent: Boolean) {

        if (!multiSelectON) {
            model.selectedThreads.clear()
        }
        model.isInMultiSelect = multiSelectON
        threadListController.toggleMultiSelectMode(multiSelectON, silent)
        scene.refreshToolbarItems()
        toggleMultiModeBar()
        scene.updateToolbarTitle(toolbarTitle)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        return when (activityMessage) {
            is ActivityMessage.SendMail -> {
                val newRequest = MailboxRequest.SendMail(activityMessage.emailId, activityMessage.threadId,
                        activityMessage.composerInputData, attachments = activityMessage.attachments)
                dataSource.submitRequest(newRequest)
                scene.showMessage(UIMessage(R.string.sending_email))
                true
            }
            is ActivityMessage.UpdateUnreadStatusThread -> {
                threadListController.updateUnreadStatusAndNotifyItem(activityMessage.threadId,
                        activityMessage.unread)
                scene.setToolbarNumberOfEmails(getTotalUnreadThreads())
                true
            }
            is ActivityMessage.MoveThread -> {
                if(activityMessage.threadId != null) {
                    threadListController.removeThreadById(activityMessage.threadId)
                    scene.setToolbarNumberOfEmails(getTotalUnreadThreads())
                }
                else{
                    reloadMailboxThreads()
                }
                true
            }
            is ActivityMessage.UpdateLabelsThread -> {
                reloadMailboxThreads()
                true
            }
            is ActivityMessage.UpdateThreadPreview -> {
                threadListController.replaceThread(activityMessage.threadPreview)
                true
            }
            else -> false
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSourceController.setDataSourceListener()
        scene.attachView(
                mailboxLabel = model.selectedLabel.text,
                threadEventListener = threadEventListener,
                onDrawerMenuItemListener = onDrawerMenuItemListener,
                observer = observer, threadList = VirtualEmailThreadList(model))
        scene.initDrawerLayout()

        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
        if (model.threads.isEmpty()) reloadMailboxThreads()

        toggleMultiModeBar()
        scene.setToolbarNumberOfEmails(getTotalUnreadThreads())
        feedController.onStart()

        websocketEvents.setListener(webSocketEventListener)

        return handleActivityMessage(activityMessage)
    }

    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
        feedController.onStop()
    }

    private fun removeCurrentLabelSelectedEmailThreads() {
        dataSourceController.updateEmailThreadsLabelsRelations(
                selectedLabels = SelectedLabels(),
                removeCurrentLabel = true)
    }

    private fun deleteSelectedEmailThreads() {
        dataSourceController.moveEmailThread(chosenLabel = MailFolders.TRASH)
    }

    private fun deleteSelectedEmailThreads4Ever() {
        scene.showDialogDeleteThread(onDeleteThreadListener)
    }

    private fun toggleReadSelectedEmailThreads(unreadStatus: Boolean) {
        val threadIds = model.selectedThreads.toList().map { it.threadId }
        dataSource.submitRequest(MailboxRequest.UpdateUnreadStatus(
                threadIds, !unreadStatus, model.selectedLabel))
        changeMode(multiSelectON = false, silent = false)
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
        if(model.isInMultiSelect){
            changeMode(multiSelectON = false, silent = false)
            threadListController.reRenderAll()
            return false
        }
        return scene.onBackPressed()
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {
        feedController.onMenuChanged(menu)
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
            R.id.mailbox_archive_selected_messages -> removeCurrentLabelSelectedEmailThreads()
            R.id.mailbox_delete_selected_messages -> deleteSelectedEmailThreads()
            R.id.mailbox_delete_selected_messages_4ever -> deleteSelectedEmailThreads4Ever()
            R.id.mailbox_not_spam -> removeCurrentLabelSelectedEmailThreads()
            R.id.mailbox_not_trash -> removeCurrentLabelSelectedEmailThreads()
            R.id.mailbox_spam -> dataSourceController.moveEmailThread(chosenLabel = MailFolders.SPAM)
            R.id.mailbox_message_toggle_read -> {
                val unreadStatus = model.isInUnreadMode
                toggleReadSelectedEmailThreads(unreadStatus = unreadStatus)
            }
            R.id.mailbox_move_to -> {
                scene.showDialogMoveTo(onMoveThreadsListener, model.selectedLabel.text)
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
        val req = MailboxRequest.GetSelectedLabels(
                threadIds = threadIds
        )

        dataSource.submitRequest(req)
        scene.showDialogLabelsChooser(LabelDataHandler(this))
    }

    fun updateEmailThreadsLabelsRelations(selectedLabels: SelectedLabels): Boolean {
        dataSourceController.updateEmailThreadsLabelsRelations(selectedLabels, false)
        return false
    }

    private val onMoveThreadsListener = object : OnMoveThreadsListener {

        override fun onMoveToInboxClicked() {
            dataSourceController.moveEmailThread(chosenLabel = MailFolders.INBOX)
        }

        override fun onMoveToSpamClicked() {
            dataSourceController.moveEmailThread(chosenLabel = MailFolders.SPAM)
        }

        override fun onMoveToTrashClicked() {
            dataSourceController.moveEmailThread(chosenLabel = MailFolders.TRASH)
        }

    }

    private val onDeleteThreadListener = object : OnDeleteThreadListener {
        override fun onDeleteConfirmed() {
            dataSourceController.moveEmailThread(chosenLabel = null)
        }
    }

    private inner class DataSourceController(
            private val dataSource: BackgroundWorkManager<MailboxRequest, MailboxResult>) {

        fun setDataSourceListener() {
            dataSource.listener = dataSourceListener
        }

        fun updateEmailThreadsLabelsRelations(
                selectedLabels: SelectedLabels,
                removeCurrentLabel: Boolean
        ): Boolean {
                val req = MailboxRequest.UpdateEmailThreadsLabelsRelations(
                        selectedThreadIds = model.selectedThreads.toList().map { it.threadId },
                        selectedLabels = selectedLabels,
                        currentLabel = model.selectedLabel,
                        shouldRemoveCurrentLabel = removeCurrentLabel)

                dataSource.submitRequest(req)
                return true
            }

        fun moveEmailThread(chosenLabel: MailFolders?) {
            val req = MailboxRequest.MoveEmailThread(
                    selectedThreadIds = model.selectedThreads.toList().map { it.threadId },
                    chosenLabel = chosenLabel,
                    currentLabel = model.selectedLabel)

            dataSource.submitRequest(req)
        }

        fun updateMailbox(mailboxLabel: Label) {
            scene.hideDrawer()
            val req = MailboxRequest.UpdateMailbox(
                    label = mailboxLabel,
                    loadedThreadsCount = model.threads.size
            )
            dataSource.submitRequest(req)
        }

        private fun handleSuccessfulMailboxUpdate(resultData: MailboxResult.UpdateMailbox.Success) {
            model.lastSync = System.currentTimeMillis()
            if (resultData.mailboxThreads != null) {
                threadListController.populateThreads(resultData.mailboxThreads)
                scene.setToolbarNumberOfEmails(getTotalUnreadThreads())
                scene.updateToolbarTitle(toolbarTitle)
                dataSource.submitRequest(MailboxRequest.GetMenuInformation())
            }
        }

        private fun handleFailedMailboxUpdate(resultData: MailboxResult.UpdateMailbox.Failure) {
            scene.showMessage(resultData.message)
        }

        fun onMailboxUpdated(resultData: MailboxResult.UpdateMailbox) {
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
            when(result) {
                is MailboxResult.LoadEmailThreads.Success -> {
                    val hasReachedEnd = result.emailPreviews.size < threadsPerPage
                    if (model.threads.isNotEmpty() && result.isReset)
                        threadListController.populateThreads(result.emailPreviews)
                    else
                        threadListController.appendAll(result.emailPreviews, hasReachedEnd)
                    scene.setToolbarNumberOfEmails(getTotalUnreadThreads())
                    if (shouldSync)
                        updateMailbox(model.selectedLabel)
                }
            }
        }

        fun onUpdatedLabels(result: MailboxResult.UpdateEmailThreadsLabelsRelations) {
            changeMode(multiSelectON = false, silent = false)
            when(result) {
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Success ->  {
                    reloadMailboxThreads()
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                } else -> {
                    scene.showMessage(UIMessage(R.string.error_updating_labels))
                }
            }
        }

        fun onMoveEmailThread(result: MailboxResult.MoveEmailThread) {
            changeMode(multiSelectON = false, silent = false)
            when(result) {
                is MailboxResult.MoveEmailThread.Success ->  {
                    reloadMailboxThreads()
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                } else -> {
                    scene.showMessage(UIMessage(R.string.error_moving_threads))
                }
            }
        }

        fun onSendMailFinished(result: MailboxResult.SendMail) {
            when (result) {
                is MailboxResult.SendMail.Success -> {
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                    reloadMailboxThreads()
                }
                is MailboxResult.SendMail.Failure -> scene.showMessage(result.message)
            }
        }

        fun onSelectedLabelsLoaded(result: MailboxResult.GetSelectedLabels) {
            when (result) {
                is MailboxResult.GetSelectedLabels.Success -> {
                    scene.onFetchedSelectedLabels(result.selectedLabels,
                            result.allLabels)
                }
                is MailboxResult.GetSelectedLabels.Failure -> {
                    scene.showMessage(UIMessage(R.string.error_getting_labels))
                }
            }
        }

        fun onGetMenuInformation(result: MailboxResult){
            when (result) {
                is MailboxResult.GetMenuInformation.Success -> {
                    scene.initNavHeader(result.account.name, "${result.account.recipientId}@${Contact.mainDomain}")
                    scene.setCounterLabel(NavigationMenuOptions.INBOX, result.totalInbox)
                    scene.setCounterLabel(NavigationMenuOptions.DRAFT, result.totalDraft)
                    scene.setCounterLabel(NavigationMenuOptions.SPAM, result.totalSpam)
                }
                is MailboxResult.GetMenuInformation.Failure -> {
                    scene.showMessage(UIMessage(R.string.error_getting_counters))
                }
            }
        }

        fun onUpdateUnreadStatus(result: MailboxResult){
            when (result) {
                is MailboxResult.UpdateUnreadStatus.Success -> {
                    reloadMailboxThreads()
                }
                is MailboxResult.UpdateUnreadStatus.Failure -> {
                    scene.showMessage(UIMessage(R.string.error_updating_status))
                }
            }
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {

        override fun onNewEmail(email: Email) {
            if (model.selectedLabel == Label.defaultItems.inbox) {
                // just reset mailbox
                val req = MailboxRequest.LoadEmailThreads(
                        label = model.selectedLabel.text,
                        loadParams = LoadParams.Reset(size = threadsPerPage),
                        userEmail = activeAccount.userEmail)
                dataSource.submitRequest(req)
            }
        }

        override fun onNewTrackingUpdate(emailId: Long, update: TrackingUpdate) {
            threadListController.markThreadAsOpened(emailId)
            feedController.reloadFeeds()
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

    val shouldSync: Boolean
            get() = System.currentTimeMillis() - model.lastSync > minimumIntervalBetweenSyncs


    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    companion object {
        val threadsPerPage = 20
        val minimumIntervalBetweenSyncs = 1000L
    }

}
