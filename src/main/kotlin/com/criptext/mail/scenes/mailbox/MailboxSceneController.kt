package com.criptext.mail.scenes.mailbox

import android.Manifest
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.*
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.data.*
import com.criptext.mail.scenes.mailbox.feed.FeedController
import com.criptext.mail.scenes.mailbox.ui.EmailThreadAdapter
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver
import com.criptext.mail.scenes.params.*
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.scenes.signin.data.LinkStatusData
import android.database.ContentObserver
import android.provider.ContactsContract
import android.view.View


/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val host: IHostActivity,
                             private val storage: KeyValueStorage,
                             private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
                             private val dataSource: BackgroundWorkManager<MailboxRequest, MailboxResult>,
                             private val activeAccount: ActiveAccount,
                             private val websocketEvents: WebSocketEventPublisher,
                             private val feedController : FeedController) : SceneController() {

    private val threadListController = ThreadListController(model, scene.virtualListView)

    private val removedDeviceDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.UpdateMailbox -> onMailboxUpdated(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.TotalUnreadEmails -> onTotalUnreadEmails(result)
            is GeneralResult.SyncPhonebook -> onSyncPhonebook(result)
        }
    }

    private val dataSourceListener = { result: MailboxResult ->
        when (result) {
            is MailboxResult.GetSelectedLabels -> dataSourceController.onSelectedLabelsLoaded(result)
            is MailboxResult.LoadEmailThreads -> dataSourceController.onLoadedMoreThreads(result)
            is MailboxResult.SendMail -> dataSourceController.onSendMailFinished(result)
            is MailboxResult.UpdateEmailThreadsLabelsRelations -> dataSourceController.onUpdatedLabels(result)
            is MailboxResult.MoveEmailThread -> dataSourceController.onMoveEmailThread(result)
            is MailboxResult.GetMenuInformation -> dataSourceController.onGetMenuInformation(result)
            is MailboxResult.UpdateUnreadStatus -> dataSourceController.onUpdateUnreadStatus(result)
            is MailboxResult.GetEmailPreview -> dataSourceController.onGetEmailPreview(result)
            is MailboxResult.EmptyTrash -> dataSourceController.onEmptyTrash(result)
            is MailboxResult.GetPendingLinkRequest -> dataSourceController.getPendingLinkRequest(result)
            is MailboxResult.ResendPeerEvents -> dataSourceController.onResendPeerEvents(result)
        }
    }

    private fun getTitleForMailbox() : String{
        return model.selectedLabel.text
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
        return model.threads.fold(0) { total, next ->
            total + (if(next.unread) 1 else 0)
        }
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
                scene.showStartGuideMultiple()
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

        override fun onCustomLabelClicked(label: Label) {
            scene.clearMenuActiveLabel()
            scene.hideDrawer()
            scene.showRefresh()
            model.selectedLabel = label
            reloadMailboxThreads()
        }

        override fun onSettingsOptionClicked() {
            host.goToScene(SettingsParams(), true)
        }

        override fun onInviteFriendOptionClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.InviteFriend())
        }

        override fun onSupportOptionClicked() {
            host.goToScene(ComposerParams(type = ComposerType.Support()), true)
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

    private val mObserver = object : ContentObserver(host.getHandler()) {

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            if(host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal,
                            Manifest.permission.READ_CONTACTS)) {
                val resolver = host.getContentResolver()
                if(resolver != null)
                    generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
            }
        }

    }

    private val dataSourceController = DataSourceController(dataSource)
    private val observer = object : MailboxUIObserver {

        override fun onWelcomeTourHasFinished() {
            scene.showSyncPhonebookDialog(this)
        }

        override fun onSyncPhonebookYes() {
            if(host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal,
                            Manifest.permission.READ_CONTACTS)) {
                storage.putBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, true)
                val resolver = host.getContentResolver()
                if(resolver != null)
                    generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
                onStartGuideEmail()
            }
        }

        override fun onStartGuideEmail(){
            if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowEmail, true)){
                scene.showStartGuideEmail()
                storage.putBool(KeyValueStorage.StringKey.StartGuideShowEmail, false)
            }
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

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

        override fun onEmptyTrashPressed() {
            scene.showEmptyTrashWarningDialog(onEmptyTrashListener)
        }

        override fun onRefreshMails() {
            dataSourceController.updateMailbox(
                    mailboxLabel = model.selectedLabel)
        }

        override fun onOpenComposerButtonClicked() {
            if(model.isInMultiSelect){
                changeMode(multiSelectON = false, silent = false)
                threadListController.reRenderAll()
            }
            val params = ComposerParams(ComposerType.Empty())
            host.goToScene(params, true)
        }

        override fun showStartGuideEmail(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_email,
                    R.dimen.focal_padding
            )
        }

        override fun showStartGuideMultiple(view: View) {
            if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowMultiple, true)){
                storage.putBool(KeyValueStorage.StringKey.StartGuideShowMultiple, false)
                host.showStartGuideView(
                        view,
                        R.string.start_guide_multiple_conversations,
                        R.dimen.focal_padding
                )
            }
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
                reloadMailboxThreads()
                val newRequest = MailboxRequest.SendMail(activityMessage.emailId, activityMessage.threadId,
                        activityMessage.composerInputData, attachments = activityMessage.attachments, fileKey = activityMessage.fileKey)
                dataSource.submitRequest(newRequest)
                scene.showMessage(UIMessage(R.string.sending_email))
                true
            }
            is ActivityMessage.UpdateUnreadStatusThread -> {
                threadListController.updateUnreadStatusAndNotifyItem(activityMessage.threadId,
                        activityMessage.unread)
                generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                true
            }
            is ActivityMessage.MoveThread -> {
                if(activityMessage.threadId != null) {
                    threadListController.removeThreadById(activityMessage.threadId)
                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
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
                generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                true
            }
            is ActivityMessage.DraftSaved -> {
                reloadMailboxThreads()
                scene.showMessage(UIMessage(R.string.draft_saved))
                true
            }
            is ActivityMessage.UpdateMailBox -> {
                reloadMailboxThreads()
                true
            }
            else -> {
                dataSource.submitRequest(MailboxRequest.ResendEmails())
                false
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSourceController.setDataSourceListener()
        generalDataSource.listener = removedDeviceDataSourceListener
        scene.attachView(
                threadEventListener = threadEventListener,
                onDrawerMenuItemListener = onDrawerMenuItemListener,
                observer = observer, threadList = VirtualEmailThreadList(model))
        scene.initDrawerLayout()

        val extras = host.getIntentExtras()

        if(extras != null) {
            when(extras.action){
                Intent.ACTION_MAIN -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasDataMail
                    dataSource.submitRequest(MailboxRequest.GetEmailPreview(threadId = extrasMail.threadId,
                            userEmail = activeAccount.userEmail))
                }
                LinkDeviceActionService.APPROVE -> {
                    val extrasDevice = extras as IntentExtrasData.IntentExtrasDataDevice
                    val untrustedDeviceInfo = UntrustedDeviceInfo(extrasDevice.deviceId, activeAccount.recipientId,
                            "", "", extrasDevice.deviceType)
                    generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
                }
                NewMailActionService.REPLY -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasReply
                    dataSource.submitRequest(MailboxRequest.GetEmailPreview(threadId = extrasMail.threadId,
                            userEmail = activeAccount.userEmail, doReply = true))
                }
                Intent.ACTION_VIEW -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasMailTo
                    host.exitToScene(ComposerParams(type = ComposerType.MailTo(extrasMail.mailTo)), null, false, true)
                }
                Intent.ACTION_APP_ERROR -> {
                    val extrasMail = extras as IntentExtrasData.IntentErrorMessage
                    scene.showMessage(extrasMail.uiMessage)
                }
                Intent.ACTION_SEND_MULTIPLE,
                Intent.ACTION_SEND -> {
                    val extrasMail = extras as IntentExtrasData.IntentExtrasSend
                    val composerMessage = ActivityMessage.AddAttachments(extrasMail.files)
                    host.exitToScene(ComposerParams(type = ComposerType.Empty()), composerMessage, false, true)
                }
            }
        }

        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
        if (model.threads.isEmpty()) reloadMailboxThreads()

        toggleMultiModeBar()
        generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
        feedController.onStart()

        websocketEvents.setListener(webSocketEventListener)
        if(model.showWelcome) {
            model.showWelcome = false
            scene.showWelcomeDialog(observer)
        }else{
            if(storage.getBool(KeyValueStorage.StringKey.ShowSyncPhonebookDialog, true)){
                scene.showSyncPhonebookDialog(observer)
                storage.putBool(KeyValueStorage.StringKey.ShowSyncPhonebookDialog, false)
            }
        }

        if(storage.getBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, false)) {
            host.getContentResolver()?.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI, true, mObserver)
        }

        dataSource.submitRequest(MailboxRequest.ResendPeerEvents())

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
        dataSourceController.moveEmailThread(chosenLabel = Label.LABEL_TRASH)
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
            R.id.mailbox_spam -> dataSourceController.moveEmailThread(chosenLabel = Label.LABEL_SPAM)
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
            dataSourceController.moveEmailThread(chosenLabel = Label.LABEL_INBOX)
        }

        override fun onMoveToSpamClicked() {
            dataSourceController.moveEmailThread(chosenLabel = Label.LABEL_SPAM)
        }

        override fun onMoveToTrashClicked() {
            dataSourceController.moveEmailThread(chosenLabel = Label.LABEL_TRASH)
        }

    }

    private val onDeleteThreadListener = object : OnDeleteThreadListener {
        override fun onDeleteConfirmed() {
            dataSourceController.moveEmailThread(chosenLabel = null)
        }
    }

    private val onEmptyTrashListener = object : OnEmptyTrashListener {
        override fun onDeleteConfirmed() {
            dataSource.submitRequest(MailboxRequest.EmptyTrash())
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

        fun moveEmailThread(chosenLabel: String?) {
            val req = MailboxRequest.MoveEmailThread(
                    selectedThreadIds = model.selectedThreads.toList().map { it.threadId },
                    chosenLabel = chosenLabel,
                    currentLabel = model.selectedLabel)

            dataSource.submitRequest(req)
        }

        fun updateMailbox(mailboxLabel: Label) {
            scene.hideDrawer()
            scene.showRefresh()
            val req = GeneralRequest.UpdateMailbox(
                    label = mailboxLabel,
                    loadedThreadsCount = model.threads.size
            )
            generalDataSource.submitRequest(req)
        }

        fun onEmptyTrash(resultData: MailboxResult.EmptyTrash) {
            when (resultData) {
                is MailboxResult.EmptyTrash.Success ->
                {
                    scene.hideEmptyTrashBanner()
                    scene.showMessage(UIMessage(R.string.trash_is_now_empty))
                    reloadMailboxThreads()
                }
                is MailboxResult.EmptyTrash.Failure ->
                    scene.showMessage(resultData.message)
                is MailboxResult.EmptyTrash.Unauthorized ->
                    generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
                is MailboxResult.EmptyTrash.Forbidden ->
                    scene.showConfirmPasswordDialog(observer)
            }
        }

        fun getPendingLinkRequest(resultData: MailboxResult.GetPendingLinkRequest) {
            when (resultData) {
                is MailboxResult.GetPendingLinkRequest.Success ->
                {
                    scene.showLinkDeviceAuthConfirmation(resultData.deviceInfo)
                }
            }
        }

        fun onResendPeerEvents(resultData: MailboxResult.ResendPeerEvents){
            when(resultData){
                is MailboxResult.ResendPeerEvents.Success -> {
                    if(!resultData.queueIsEmpty){
                        val handler = Handler()
                        handler.postDelayed({
                            dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
                        }, TIME_TO_RESEND_EVENTS)
                    }
                }
                is MailboxResult.ResendPeerEvents.Failure -> {
                    if(!resultData.queueIsEmpty){
                        val handler = Handler()
                        handler.postDelayed({
                            dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
                        }, TIME_TO_RESEND_EVENTS)
                    }
                }
            }
        }

        fun onLoadedMoreThreads(result: MailboxResult.LoadEmailThreads) {
            scene.clearRefreshing()
            scene.updateToolbarTitle(toolbarTitle)
            when(result) {
                is MailboxResult.LoadEmailThreads.Success -> {
                    val hasReachedEnd = result.emailPreviews.size < threadsPerPage
                    when(result.loadParams){
                        is LoadParams.Reset -> {
                            if (model.threads.isNotEmpty())
                                threadListController.populateThreads(result.emailPreviews)
                            else
                                threadListController.appendAll(result.emailPreviews, hasReachedEnd)
                        }
                        is LoadParams.NewPage ->
                            threadListController.appendAll(result.emailPreviews, hasReachedEnd)
                        is LoadParams.UpdatePage -> {
                            val newEmails = model.threads.filter { (it !in result.emailPreviews)
                                    && (it.timestamp.after(model.threads.first().timestamp)) }
                            val oldEmails = model.threads.filter { it in result.emailPreviews }
                            threadListController.updateThreadsAndAddNew(newEmails, oldEmails)
                            if(newEmails.isNotEmpty() && !threadListController.isOnTopOfList())
                                scene.showMessage(UIMessage(R.string.new_email_snack, arrayOf(newEmails.size)))
                        }
                    }

                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    if (shouldSync)
                        updateMailbox(model.selectedLabel)
                    if(result.mailboxLabel == Label.LABEL_TRASH &&
                            (result.emailPreviews.isNotEmpty() || model.threads.isNotEmpty())){
                        scene.showEmptyTrashBanner()
                    }else{
                        scene.hideEmptyTrashBanner()
                    }
                }
            }
        }

        fun onUpdatedLabels(result: MailboxResult.UpdateEmailThreadsLabelsRelations) {
            changeMode(multiSelectON = false, silent = false)
            when(result) {
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Success ->  {
                    threadListController.updateThreadLabels(result.threadIds, result.isStarred)
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                }
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Failure -> {
                    threadListController.reRenderAll()
                    scene.showMessage(UIMessage(R.string.error_updating_labels))
                }
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
                }
                is MailboxResult.UpdateEmailThreadsLabelsRelations.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
            }
        }

        fun onMoveEmailThread(result: MailboxResult.MoveEmailThread) {
            changeMode(multiSelectON = false, silent = false)
            when(result) {
                is MailboxResult.MoveEmailThread.Success ->  {
                    if(model.selectedLabel.text != Label.LABEL_ALL_MAIL ||
                            result.chosenLabel == Label.LABEL_SPAM ||
                            result.chosenLabel == Label.LABEL_TRASH) {
                        threadListController.removeThreadsById(result.threadIds)
                        feedController.reloadFeeds()
                        generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    }else{
                        threadListController.reRenderAll()
                    }
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                }
                is MailboxResult.MoveEmailThread.Failure -> {
                    threadListController.reRenderAll()
                    scene.showMessage(UIMessage(R.string.error_moving_threads))
                }
                is MailboxResult.MoveEmailThread.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
                }
                is MailboxResult.MoveEmailThread.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
            }
        }

        fun onSendMailFinished(result: MailboxResult.SendMail) {
            when (result) {
                is MailboxResult.SendMail.Success -> {
                    if(result.emailId != null) {
                        scene.showMessage(UIMessage(R.string.email_sent))
                        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                        reloadMailboxThreads()
                    }
                }
                is MailboxResult.SendMail.Failure -> {
                    scene.showMessage(result.message)
                }
                is MailboxResult.SendMail.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
                }
                is MailboxResult.SendMail.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
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
                    scene.setMenuLabels(result.labels.map { LabelWrapper(it) })
                }
                is MailboxResult.GetMenuInformation.Failure -> {
                    scene.showMessage(UIMessage(R.string.error_getting_counters))
                }
            }
        }

        fun onUpdateUnreadStatus(result: MailboxResult){
            when (result) {
                is MailboxResult.UpdateUnreadStatus.Success -> {
                    threadListController.changeThreadReadStatus(result.threadId, result.unreadStatus)
                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                }
                is MailboxResult.UpdateUnreadStatus.Failure -> {
                    threadListController.reRenderAll()
                    scene.showMessage(UIMessage(R.string.error_updating_status))
                }
                is MailboxResult.UpdateUnreadStatus.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
                }
                is MailboxResult.UpdateUnreadStatus.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
            }
        }

        fun onGetEmailPreview(result: MailboxResult){
            when (result) {
                is MailboxResult.GetEmailPreview.Success -> {
                    reloadMailboxThreads()
                    feedController.reloadFeeds()
                    host.goToScene(EmailDetailParams(threadId = result.emailPreview.threadId,
                            currentLabel = model.selectedLabel, threadPreview = result.emailPreview,
                            doReply = result.doReply), true)
                }
                is MailboxResult.GetEmailPreview.Failure -> {
                    dataSourceController.updateMailbox(model.selectedLabel)
                }
            }
        }
    }

    private fun handleSuccessfulMailboxUpdate(resultData: GeneralResult.UpdateMailbox.Success) {
        model.lastSync = System.currentTimeMillis()
        if (resultData.mailboxThreads != null) {
            threadListController.populateThreads(resultData.mailboxThreads)
            generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
            scene.updateToolbarTitle(toolbarTitle)
            dataSource.submitRequest(MailboxRequest.GetMenuInformation())
            feedController.reloadFeeds()
            dataSource.submitRequest(MailboxRequest.ResendEmails())
        }
    }

    private fun handleFailedMailboxUpdate(resultData: GeneralResult.UpdateMailbox.Failure) {
        scene.showMessage(resultData.message)
    }

    private fun onTotalUnreadEmails(resultData: GeneralResult.TotalUnreadEmails){
        when (resultData) {
            is GeneralResult.TotalUnreadEmails.Success -> {
                scene.setToolbarNumberOfEmails(resultData.total)
            }
        }
    }

    private fun onSyncPhonebook(resultData: GeneralResult.SyncPhonebook){
        when (resultData) {
            is GeneralResult.SyncPhonebook.Success -> {
                scene.showMessage(UIMessage(R.string.sync_phonebook_text))
            }
        }
    }

    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.exitToScene(LinkingParams(activeAccount.userEmail, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), null,
                        false, true)
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onMailboxUpdated(resultData: GeneralResult.UpdateMailbox) {
        scene.clearRefreshing()
        when (resultData) {
            is GeneralResult.UpdateMailbox.Success -> {
                handleSuccessfulMailboxUpdate(resultData)
                dataSource.submitRequest(MailboxRequest.GetPendingLinkRequest())
            }
            is GeneralResult.UpdateMailbox.Failure -> {
                handleFailedMailboxUpdate(resultData)
                dataSource.submitRequest(MailboxRequest.GetPendingLinkRequest())
            }
            is GeneralResult.UpdateMailbox.Unauthorized ->
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            is GeneralResult.UpdateMailbox.Forbidden ->
                scene.showConfirmPasswordDialog(observer)
        }
        dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                        true, true)
            }
        }
    }

    private fun onPasswordChangedRemotely(result: GeneralResult.ConfirmPassword){
        when (result) {
            is GeneralResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is GeneralResult.ConfirmPassword.Failure -> {
                scene.setConfirmPasswordError(result.message)
            }
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onNewEvent() {
            reloadViewAfterSocketEvent()
        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(observer)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }

        private fun reloadViewAfterSocketEvent(){
            val req = MailboxRequest.LoadEmailThreads(
                    label = model.selectedLabel.text,
                    loadParams = LoadParams.UpdatePage(size = model.threads.size,
                            mostRecentDate = model.threads.firstOrNull()?.timestamp),
                    userEmail = activeAccount.userEmail)
            dataSource.submitRequest(req)
            feedController.reloadFeeds()
            dataSource.submitRequest(MailboxRequest.GetMenuInformation())
        }
    }

    val shouldSync: Boolean
            get() = System.currentTimeMillis() - model.lastSync > minimumIntervalBetweenSyncs


    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.readAccess.ordinal) return

        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.READ_CONTACTS }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
            scene.showMessage(UIMessage(R.string.sync_phonebook_permission))
            return
        }
        val resolver = host.getContentResolver()
        if(resolver != null)
            generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
    }

    companion object {
        val threadsPerPage = 20
        val minimumIntervalBetweenSyncs = 1000L
        const val TIME_TO_RESEND_EVENTS = 5000L
    }

}
