package com.criptext.mail.scenes.mailbox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.provider.ContactsContract
import android.view.View
import com.criptext.mail.*
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.push.services.SyncDeviceActionService
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.data.*
import com.criptext.mail.scenes.mailbox.feed.FeedController
import com.criptext.mail.scenes.mailbox.ui.EmailThreadAdapter
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.IntentUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.UIUtils
import com.criptext.mail.utils.eventhelper.ParsedEvent
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import com.g00fy2.versioncompare.Version
import com.google.api.services.drive.Drive
import java.io.File

/**
 * Created by sebas on 1/30/18.
 */
class MailboxSceneController(private val scene: MailboxScene,
                             private val model: MailboxSceneModel,
                             private val host: IHostActivity,
                             private val storage: KeyValueStorage,
                             private val generalDataSource: GeneralDataSource,
                             private val dataSource: MailboxDataSource,
                             private var activeAccount: ActiveAccount,
                             private var websocketEvents: WebSocketEventPublisher,
                             private val feedController : FeedController) : SceneController() {

    private val threadListController = ThreadListController(model, scene.virtualListView)

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.Logout -> onLogout(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.BackgroundAccountsUpdateMailbox -> onBackgroundAccountsMailboxUpdated(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.TotalUnreadEmails -> onTotalUnreadEmails(result)
            is GeneralResult.SyncPhonebook -> onSyncPhonebook(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.GetRemoteFile -> onGetRemoteFile(result)
            is GeneralResult.ActiveAccountUpdateMailbox -> onActiveAccountUpdateMailbox(result)
            is GeneralResult.GetEmailPreview -> dataSourceController.onGetEmailPreview(result)
            is GeneralResult.SetActiveAccountFromPush -> dataSourceController.onSetActiveAccountFromPush(result)
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
            is MailboxResult.EmptyTrash -> dataSourceController.onEmptyTrash(result)
            is MailboxResult.ResendPeerEvents -> dataSourceController.onResendPeerEvents(result)
            is MailboxResult.SetActiveAccount -> dataSourceController.onSetActiveAccount(result)
        }
    }

    private fun getTitleForMailbox() : String{
        val uiMessage = UIUtils.getLocalizedSystemLabelName(model.selectedLabel.text)
        return host.getLocalizedString(uiMessage).toUpperCase()
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

    private fun reloadMailboxThreads(loadParams: LoadParams = LoadParams.Reset(threadsPerPage)) {
        val req = MailboxRequest.LoadEmailThreads(
                label = model.selectedLabel.text,
                filterUnread = model.showOnlyUnread,
                loadParams = loadParams,
                userEmail = activeAccount.userEmail)
        dataSource.submitRequest(req)
    }

    private val threadEventListener = object : EmailThreadAdapter.OnThreadEventListener {
        override fun onApproachingEnd() {
                val req = MailboxRequest.LoadEmailThreads(
                        label = model.selectedLabel.text,
                        filterUnread = model.showOnlyUnread,
                        loadParams = LoadParams.NewPage(size = threadsPerPage,
                        startDate = model.threads.lastOrNull()?.timestamp),
                        userEmail = activeAccount.userEmail)
                dataSource.submitRequest(req)
        }

        override fun onGoToMail(emailPreview: EmailPreview) {

            if(emailPreview.count == 1 &&
                    model.selectedLabel == Label.defaultItems.draft){
                val params = ComposerParams(ComposerType.Draft(draftId = emailPreview.emailId,
                        threadPreview = emailPreview, currentLabel = model.selectedLabel), model.selectedLabel)
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
        override fun onProfileClicked() {
            host.goToScene(ProfileParams(true), true)
        }

        override fun onAccountClicked(account: Account) {
            scene.hideDrawer()
            model.extraAccountHasUnreads = false
            scene.showExtraAccountsBadge(false)
            scene.hideMultipleAccountsMenu()
            dataSource.submitRequest(MailboxRequest.SetActiveAccount(account))
        }

        override fun onAddAccountClicked() {
            scene.hideMultipleAccountsMenu()
            host.goToScene(SignInParams(true), true)
        }

        override fun onCustomLabelClicked(label: Label) {
            model.showOnlyUnread = false
            scene.clearMenuActiveLabel()
            scene.hideDrawer()
            scene.showRefresh()
            model.selectedLabel = label
            scene.setEmtpyMailboxBackground(Label.defaultItems.inbox)
            reloadMailboxThreads()
        }

        override fun onSettingsOptionClicked() {
            host.goToScene(SettingsParams(), true)
        }

        override fun onInviteFriendOptionClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.InviteFriend())
        }

        override fun onSupportOptionClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("help-desk"))
        }

        override fun onNavigationItemClick(navigationMenuOptions: NavigationMenuOptions) {
            scene.hideDrawer()
            scene.hideUpdateBanner()

            when(navigationMenuOptions) {
                NavigationMenuOptions.INBOX,
                NavigationMenuOptions.SENT,
                NavigationMenuOptions.DRAFT,
                NavigationMenuOptions.STARRED,
                NavigationMenuOptions.SPAM,
                NavigationMenuOptions.TRASH,
                NavigationMenuOptions.ALL_MAIL -> {
                    model.showOnlyUnread = false
                    host.refreshToolbarItems()
                    scene.showRefresh()
                    model.selectedLabel = navigationMenuOptions.toLabel()!!
                    scene.setEmtpyMailboxBackground(model.selectedLabel)
                    reloadMailboxThreads()
                }
                else -> { /* do nothing */ }
            }

        }
    }

    val googleSignInListener = object: GoogleSignInObserver {
        override fun signInSuccess(drive: Drive){
            model.mDriveServiceHelper = drive
            host.goToScene(RestoreBackupParams(), false)
        }

        override fun signInFailed(){
            scene.showMessage(UIMessage(R.string.login_fail_try_again_error_exception))
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
        override fun restoreFromLocalBackupPressed() {
            if(host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
            }
        }

        override fun restoreFromBackupPressed() {
            val gAccount = scene.getGoogleDriveService()
            if (gAccount == null){
                host.launchExternalActivityForResult(ExternalActivityParams.SignInGoogleDrive())
            } else {
                model.mDriveServiceHelper = gAccount
                host.goToScene(RestoreBackupParams(), false)
            }
        }

        override fun onSnackbarClicked() {
            scene.scrollTop()
        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
            else
                scene.showToastMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn ->
                            host.goToScene(SignInParams(true), true)
                    }
                }
            }
        }
        
        override fun onUpdateBannerXPressed() {
            scene.hideUpdateBanner()
        }

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

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
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
                if(model.extraAccountHasUnreads)
                    scene.showExtraAccountsBadge(true)
            }
        }

        override fun onEmptyTrashPressed() {
            scene.showEmptyTrashWarningDialog(onEmptyTrashListener)
        }

        override fun onRefreshMails() {
            if (shouldSync) {
                dataSourceController.updateMailbox(
                        mailboxLabel = model.selectedLabel)
                updateBackgroundAccounts()
            }
        }

        override fun onOpenComposerButtonClicked() {
            model.showOnlyUnread = false
            if(model.isInMultiSelect){
                changeMode(multiSelectON = false, silent = false)
                threadListController.reRenderAll()
            }
            val params = ComposerParams(ComposerType.Empty(), model.selectedLabel)
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

        override fun showSecureIconGuide(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_secure,
                    R.dimen.focal_padding_attachments
            )
        }
    }

    val menuClickListener = {
        scene.openNotificationFeed()
    }

    private fun updateBackgroundAccounts(){
        model.lastSyncBackground = System.currentTimeMillis()
        generalDataSource.submitRequest(GeneralRequest.BackgroundAccountsUpdateMailbox(
                label = model.selectedLabel,
                accounts = model.extraAccounts
        ))
    }


    fun changeMode(multiSelectON: Boolean, silent: Boolean) {

        if (!multiSelectON) {
            model.selectedThreads.clear()
            if(model.extraAccountHasUnreads)
                scene.showExtraAccountsBadge(true)
        }
        scene.showExtraAccountsBadge(false)
        model.isInMultiSelect = multiSelectON
        threadListController.toggleMultiSelectMode(multiSelectON, silent)
        scene.refreshToolbarItems()
        toggleMultiModeBar()
        scene.hideComposer(multiSelectON)
        scene.updateToolbarTitle(toolbarTitle)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?, isResume: Boolean = false): Boolean {
        return when (activityMessage) {
            is ActivityMessage.SendMail -> {
                val newRequest = MailboxRequest.SendMail(activityMessage.emailId, activityMessage.threadId,
                        currentLabel = model.selectedLabel,
                        data = activityMessage.composerInputData, attachments = activityMessage.attachments,
                        fileKey = activityMessage.fileKey, senderAccount = activityMessage.senderAccount)
                dataSource.submitRequest(newRequest)
                scene.showMessage(UIMessage(R.string.sending_email))
                true
            }
            is ActivityMessage.LogoutAccount -> {
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }.toMutableList()
                val oldAccount = model.extraAccounts.find {
                    it.recipientId.plus("@${it.domain}") == activityMessage.oldAccountEmail
                }
                if(oldAccount != null) {
                    jwts.remove(oldAccount.jwt)
                    storage.putString(KeyValueStorage.StringKey.JWTS, jwts.joinToString())

                }
                activateAccount(activityMessage.newAccount)
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
                reloadMailboxThreads(LoadParams.UpdatePage(size = model.threads.size,
                        mostRecentDate = model.threads.firstOrNull()?.timestamp))
                true
            }
            is ActivityMessage.UpdateThreadPreview -> {
                threadListController.replaceThread(activityMessage.threadPreview)
                generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                model.threads.sortByDescending { it.timestamp }
                threadListController.reRenderAll()
                true
            }
            is ActivityMessage.DraftSaved -> {
                if(activityMessage.preview != null) {
                    threadListController.replaceThread(activityMessage.preview)
                }
                model.threads.sortByDescending { it.timestamp }
                threadListController.reRenderAll()
                scene.showMessage(UIMessage(R.string.draft_saved))
                true
            }
            is ActivityMessage.UpdateMailBox -> {
                reloadMailboxThreads()
                true
            }
            is ActivityMessage.ShowUIMessage -> {
                scene.showMessage(activityMessage.message)
                true
            }
            is ActivityMessage.AddAttachments -> {
                val file = activityMessage.filesMetadata.firstOrNull() ?: return true
                if(file.second == -1L) {
                    val resolver = host.getContentResolver()
                    if(resolver != null) {
                        scene.showPreparingFileDialog()
                        generalDataSource.submitRequest(GeneralRequest.GetRemoteFile(
                                listOf(file.first), resolver)
                        )
                    }
                } else {
                    val isFileEncrypted = File(file.first).extension == UserDataWriter.FILE_ENCRYPTED_EXTENSION
                    host.exitToScene(RestoreBackupParams(true, Pair(file.first, isFileEncrypted)), null, false, true)
                }
                return true
            }
            else -> {
                dataSource.submitRequest(MailboxRequest.ResendEmails())
                if(isResume)
                    reloadMailboxThreads()
                false
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSourceController.setDataSourceListener()
        generalDataSource.listener = generalDataSourceListener
        scene.attachView(
                threadEventListener = threadEventListener,
                onDrawerMenuItemListener = onDrawerMenuItemListener,
                observer = observer, threadList = VirtualEmailThreadList(model),
                email = activeAccount.userEmail, fullName = activeAccount.name)
        scene.initDrawerLayout()
        scene.setEmtpyMailboxBackground(model.selectedLabel)

        val extras = host.getIntentExtras()

        if(extras != null) {
            if(extras.account == activeAccount.recipientId && extras.domain == activeAccount.domain)
                IntentUtils.handleIntentExtras(extras, generalDataSource, activeAccount, host, model.selectedLabel)
            else {
                model.waitForAccountSwitch = true
                generalDataSource.submitRequest(GeneralRequest.SetActiveAccountFromPush(extras.account, extras.domain, extras))
            }
        }

        if(!model.waitForAccountSwitch) {

            dataSource.submitRequest(MailboxRequest.GetMenuInformation())
            if (model.threads.isEmpty())
                reloadMailboxThreads()
            else
                dataSourceController.updateMailbox(model.selectedLabel)

            toggleMultiModeBar()
            feedController.onStart()

            websocketEvents.setListener(webSocketEventListener)
            if (model.showWelcome) {
                model.showWelcome = false
                scene.showWelcomeDialog(observer)
            } else {
                if(model.askForRestoreBackup){
                    model.askForRestoreBackup = false
                    scene.showRestoreBackupDialog(observer)
                } else {
                    if (storage.getBool(KeyValueStorage.StringKey.ShowSyncPhonebookDialog, true)) {
                        scene.showSyncPhonebookDialog(observer)
                        storage.putBool(KeyValueStorage.StringKey.ShowSyncPhonebookDialog, false)
                    }
                }
            }

            if (storage.getBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, false)) {
                if (host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal,
                                Manifest.permission.READ_CONTACTS)) {
                    host.getContentResolver()?.registerContentObserver(
                            ContactsContract.Contacts.CONTENT_URI, true, mObserver)
                }
            }

            dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
        }
        scene.checkRating(storage)

        return handleActivityMessage(activityMessage)
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return handleActivityMessage(activityMessage, true)
    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    override fun onPause() {
        cleanup(false)
    }

    override fun onStop() {
        cleanup(true)
    }

    private fun cleanup(fullCleanup: Boolean){
        websocketEvents.clearListener(webSocketEventListener)

        if(fullCleanup) {
            feedController.onStop()
        }
    }

    private fun removeCurrentLabelSelectedEmailThreads() {
        dataSourceController.updateEmailThreadsLabelsRelations(
                selectedLabels = SelectedLabels(),
                removeCurrentLabel = true)
        reloadMailboxThreads()
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
        threadListController.changeThreadReadStatus(threadIds, !unreadStatus)
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
        val exit = scene.onBackPressed()
        if(exit) threadListController.clear()
        return exit
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {
        feedController.onMenuChanged(menu)
    }

    private fun toggleMultiModeBar() {
        if (model.isInMultiSelect) {
            showMultiModeBar()
        } else {
            hideMultiModeBar()
            if(model.extraAccountHasUnreads)
                scene.showExtraAccountsBadge(true)
        }
    }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            R.id.mailbox_search -> host.goToScene(SearchParams(), true)
            R.id.mailbox_filter_unread -> {
                model.showOnlyUnread = true
                model.threads.clear()
                reloadMailboxThreads()
            }
            R.id.mailbox_filter_none -> {
                model.showOnlyUnread = false
                model.threads.clear()
                reloadMailboxThreads()
            }
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
            reloadMailboxThreads(LoadParams.UpdatePage(size = model.threads.size,
                    mostRecentDate = model.threads.firstOrNull()?.timestamp))
        }
    }

    private val onEmptyTrashListener = object : OnEmptyTrashListener {
        override fun onDeleteConfirmed() {
            dataSource.submitRequest(MailboxRequest.EmptyTrash())
            reloadMailboxThreads()
        }
    }

    private inner class DataSourceController(
            private val dataSource: MailboxDataSource) {

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
            val req = GeneralRequest.ActiveAccountUpdateMailbox(
                    label = mailboxLabel
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
                    generalDataSource.submitRequest(GeneralRequest.Logout(false))
                is MailboxResult.EmptyTrash.Forbidden ->
                    scene.showConfirmPasswordDialog(observer)
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

        fun onSetActiveAccount(resultData: MailboxResult.SetActiveAccount){
            when(resultData){
                is MailboxResult.SetActiveAccount.Success -> {
                    activeAccount = resultData.activeAccount
                    generalDataSource.activeAccount = activeAccount
                    dataSource.activeAccount = activeAccount

                    val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                    websocketEvents = if(jwts.isNotEmpty())
                        WebSocketSingleton.getInstance(jwts)
                    else
                        WebSocketSingleton.getInstance(activeAccount.jwt)

                    websocketEvents.setListener(webSocketEventListener)
                    generalDataSource.listener = null
                    dataSource.listener = null
                    host.exitToScene(MailboxParams(),
                            ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            true, false)
                }
            }
        }

        fun onSetActiveAccountFromPush(resultData: GeneralResult.SetActiveAccountFromPush){
            when(resultData){
                is GeneralResult.SetActiveAccountFromPush.Success -> {
                    activeAccount = resultData.activeAccount
                    generalDataSource.activeAccount = activeAccount
                    dataSource.activeAccount = activeAccount

                    IntentUtils.handleIntentExtras(resultData.extrasData, generalDataSource, activeAccount,
                            host, model.selectedLabel, true)

                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                    if (model.threads.isEmpty()) reloadMailboxThreads()

                    toggleMultiModeBar()
                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    feedController.onStart()

                    websocketEvents.setListener(webSocketEventListener)
                    dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
                    scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))
                }
            }
        }

        fun onLoadedMoreThreads(result: MailboxResult.LoadEmailThreads) {
            scene.clearRefreshing()
            scene.updateToolbarTitle(toolbarTitle)
            when(result) {
                is MailboxResult.LoadEmailThreads.Success -> {
                    if(model.threads.isEmpty())
                        dataSourceController.updateMailbox(model.selectedLabel)
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
                            threadListController.populateThreads(result.emailPreviews)
                        }
                    }

                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    if(result.mailboxLabel == Label.LABEL_TRASH &&
                            (result.emailPreviews.isNotEmpty() || model.threads.isNotEmpty())){
                        scene.showEmptyTrashBanner()
                    }else{
                        scene.hideEmptyTrashBanner()
                    }
                }
            }
            scene.setEmtpyMailboxBackground(model.selectedLabel)
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
                    generalDataSource.submitRequest(GeneralRequest.Logout(false))
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
                    } else {
                        threadListController.reRenderAll()
                    }
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                }
                is MailboxResult.MoveEmailThread.Failure -> {
                    threadListController.reRenderAll()
                    scene.showMessage(UIMessage(R.string.error_moving_threads))
                }
                is MailboxResult.MoveEmailThread.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.Logout(false))
                }
                is MailboxResult.MoveEmailThread.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
            }
        }

        fun onSendMailFinished(result: MailboxResult.SendMail) {
            when (result) {
                is MailboxResult.SendMail.Success -> {
                    if(result.newEmailPreview != null) {
                        if(result.isSecure){
                            scene.showMessage(UIMessage(R.string.email_sent_secure))
                        } else {
                            scene.showMessage(UIMessage(R.string.email_sent))
                        }
                        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                        threadListController.replaceThread(result.newEmailPreview.copy(deliveryStatus = DeliveryTypes.SENT))
                        model.threads.sortByDescending { it.timestamp }
                        threadListController.reRenderAll()
                    }
                }
                is MailboxResult.SendMail.Failure -> {
                    scene.showMessage(result.message)
                }
                is MailboxResult.SendMail.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.Logout(false))
                }
                is MailboxResult.SendMail.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
                is MailboxResult.SendMail.EnterpriseSuspended -> {
                    showSuspendedAccountDialog()
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
                    scene.initNavHeader(result.account.name, "${result.account.recipientId}@${result.account.domain}")
                    scene.setCounterLabel(NavigationMenuOptions.INBOX, result.totalInbox)
                    scene.setCounterLabel(NavigationMenuOptions.DRAFT, result.totalDraft)
                    scene.setCounterLabel(NavigationMenuOptions.SPAM, result.totalSpam)
                    model.extraAccounts = result.accounts.filter { !it.isActive }
                    scene.setMenuLabels(result.labels.map { LabelWrapper(it) })
                    scene.setMenuAccounts(model.extraAccounts, model.extraAccounts.map { 0 })
                    val position = model.threads.indexOfFirst { it.isSecure }
                    if(position > -1){
                        val secureLockHasShown = storage.getBool(KeyValueStorage.StringKey.StartGuideShowSecureEmail, false)
                        if(!secureLockHasShown) {
                            scene.showSecureLockGuide(position)
                            storage.putBool(KeyValueStorage.StringKey.StartGuideShowSecureEmail, true)
                        }
                    }
                }
                is MailboxResult.GetMenuInformation.Failure -> {
                    scene.showMessage(UIMessage(R.string.error_getting_counters))
                }
            }
        }

        fun onUpdateUnreadStatus(result: MailboxResult){
            when (result) {
                is MailboxResult.UpdateUnreadStatus.Success -> {
                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                }
                is MailboxResult.UpdateUnreadStatus.Failure -> {
                    threadListController.reRenderAll()
                    scene.showMessage(UIMessage(R.string.error_updating_status))
                }
                is MailboxResult.UpdateUnreadStatus.Unauthorized -> {
                    generalDataSource.submitRequest(GeneralRequest.Logout(false))
                }
                is MailboxResult.UpdateUnreadStatus.Forbidden -> {
                    scene.showConfirmPasswordDialog(observer)
                }
            }
        }

        fun onGetEmailPreview(result: GeneralResult){
            when (result) {
                is GeneralResult.GetEmailPreview.Success -> {
                    reloadMailboxThreads()
                    feedController.reloadFeeds()
                    host.goToScene(EmailDetailParams(threadId = result.emailPreview.threadId,
                            currentLabel = model.selectedLabel, threadPreview = result.emailPreview,
                            doReply = result.doReply), true, activityMessage = result.activityMessage)
                }
                is GeneralResult.GetEmailPreview.Failure -> {
                    dataSourceController.updateMailbox(model.selectedLabel)
                    updateBackgroundAccounts()
                }
            }
        }
    }

    private fun activateAccount(newActiveAccount: ActiveAccount){
        activeAccount = newActiveAccount
        generalDataSource.activeAccount = activeAccount
        dataSource.activeAccount = activeAccount

        val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
        websocketEvents = if(jwts.isNotEmpty())
            WebSocketSingleton.getInstance(jwts)
        else
            WebSocketSingleton.getInstance(activeAccount.jwt)

        websocketEvents.setListener(webSocketEventListener)

        scene.initMailboxAvatar(activeAccount.name, activeAccount.userEmail)

        scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))

        model.selectedLabel = Label.defaultItems.inbox

        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
        reloadMailboxThreads()

        toggleMultiModeBar()
        generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
        feedController.onStart()
    }

    private fun handleActiveAccountSuccessfulMailboxUpdate(resultData: GeneralResult.ActiveAccountUpdateMailbox.Success) {
        model.lastSync = System.currentTimeMillis()
        if (resultData.data != null) {
            resultData.data.parsedEvents.forEach {
                when(it.cmd){
                    Event.Cmd.deviceAuthRequest,
                    Event.Cmd.syncBeginRequest -> {
                        val linkEvent = it as ParsedEvent.LinkDeviceInfo
                        handleSyncEvents(listOf(linkEvent.deviceInfo))
                    }
                    Event.Cmd.updateBannerEvent -> {
                        val bannerEvent = it as ParsedEvent.BannerData
                        handleBanner(bannerEvent.updateBannerData)
                    }
                    Event.Cmd.newEmail -> {
                        val newEmailEvent = it as ParsedEvent.NewEmail
                        threadListController.addNew(newEmailEvent.preview)
                    }
                    Event.Cmd.peerEmailThreadReadStatusUpdate -> {
                        val threadReadEvent = it as ParsedEvent.ReadThreads
                        threadListController.changeThreadsReadStatus(listOf(threadReadEvent.threadRead))
                    }
                    Event.Cmd.peerEmailReadStatusUpdate -> {
                        val emailReadEvent = it as ParsedEvent.ReadEmails
                        threadListController.changeEmailsReadStatus(listOf(emailReadEvent.emailRead))
                    }
                    Event.Cmd.peerEmailUnsendStatusUpdate -> {
                        val unsendEvent = it as ParsedEvent.UnsendEmail
                        val thread = model.threads.find {preview ->
                            preview.metadataKey == unsendEvent.unsend.first
                        }
                        if(thread != null)
                            threadListController.changeUnsendStatus(thread.threadId, unsendEvent.unsend.second)
                    }
                    Event.Cmd.peerEmailChangedLabels,
                    Event.Cmd.peerThreadChangedLabels,
                    Event.Cmd.peerEmailDeleted -> {
                        reloadMailboxThreads(LoadParams.UpdatePage(size = model.threads.size,
                                mostRecentDate = model.threads.firstOrNull()?.timestamp))
                    }
                    Event.Cmd.peerThreadDeleted -> {
                        val deleteThread = it as ParsedEvent.MoveThread
                        threadListController.removeThreadsById(deleteThread.threadIds)
                    }
                    Event.Cmd.peerLabelCreated,
                    Event.Cmd.peerLabelEdited,
                    Event.Cmd.peerLabelDeleted -> {
                        val labelsEvent = it as ParsedEvent.ChangeToLabels
                        scene.setMenuLabels(labelsEvent.customLabels.map { label ->  LabelWrapper(label) })
                    }
                    Event.Cmd.trackingUpdate -> {
                        val trackingUpdate = it as ParsedEvent.TrackingEvent
                        threadListController.changeEmailsDeliveryStatus(listOf(trackingUpdate.trackingUpdate))
                    }
                    Event.Cmd.peerUserChangeName -> {
                        dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                    }
                    Event.Cmd.profilePictureChanged -> {
                        scene.initMailboxAvatar(activeAccount.name, activeAccount.userEmail)
                        scene.initNavHeader(activeAccount.name, activeAccount.userEmail)
                    }
                }
            }
            val newEmails = resultData.data.parsedEvents.filterIsInstance<ParsedEvent.NewEmail>()
            if(newEmails.isNotEmpty()){
                if(resultData.shouldNotify){
                    scene.showNotification()
                    if(!threadListController.isOnTopOfList())
                        scene.showMessage(UIMessage(R.string.new_email_snack, arrayOf(newEmails.size)), true)
                }
                generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
            }
            if(resultData.data.parsedEvents.find { it is ParsedEvent.MoveThread || it is ParsedEvent.MoveEmail } != null){
                generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                dataSource.submitRequest(MailboxRequest.GetMenuInformation())
            }
            val isSelected = model.threads.filter { it.isSelected }
            val selectedPositions = model.threads.filter {
                modelThread -> modelThread.threadId in isSelected.map { it.threadId }
            }.map { model.threads.indexOf(it) }
            if(model.isInMultiSelect){
                model.selectedThreads.clear()
                selectedPositions.forEach {
                    if(it > -1) {
                        model.threads[it].isSelected = true
                        threadListController.select(model.threads[it], it)
                    }
                }
            }
            feedController.reloadFeeds()
        }
    }

    private fun handleBanner(updateBannerData: UpdateBannerData){
        fun sendUpdateNowBanner(){
            val newBannerData = UpdateBannerData(
                    title = host.getLocalizedString(UIMessage(R.string.update_now_title)),
                    message = host.getLocalizedString(UIMessage(R.string.update_now_message)),
                    image = updateBannerData.image,
                    version = updateBannerData.version,
                    operator = updateBannerData.operator
            )
            scene.showUpdateBanner(newBannerData)
            scene.setUpdateBannerClick()
        }

        fun sendUpdateBanner(){
            scene.clearUpdateBannerClick()
            scene.showUpdateBanner(updateBannerData)
        }

        when(updateBannerData.operator){
            1 -> {
                if(Version(BuildConfig.VERSION_NAME).isLowerThan(updateBannerData.version)){
                    sendUpdateBanner()
                }
            }
            2 -> {
                if(Version(BuildConfig.VERSION_NAME).isLowerThan(updateBannerData.version)
                        || BuildConfig.VERSION_NAME == updateBannerData.version){
                    sendUpdateBanner()
                }
            }
            3 -> {
                if(BuildConfig.VERSION_NAME == updateBannerData.version){
                    sendUpdateBanner()
                }
            }
            4 -> {
                if(Version(BuildConfig.VERSION_NAME).isHigherThan(updateBannerData.version)
                        || BuildConfig.VERSION_NAME == updateBannerData.version){
                    sendUpdateBanner()
                }else{
                    sendUpdateNowBanner()
                }
            }
            5 -> {
                if(Version(BuildConfig.VERSION_NAME).isHigherThan(updateBannerData.version)){
                    sendUpdateBanner()
                }else{
                    sendUpdateNowBanner()
                }
            }
        }
    }

    private fun handleSyncEvents(syncEventsList: List<DeviceInfo?>){
        syncEventsList.forEach {
            when(it){
                is DeviceInfo.TrustedDeviceInfo ->
                    scene.showSyncDeviceAuthConfirmation(it)
                is DeviceInfo.UntrustedDeviceInfo ->
                    scene.showLinkDeviceAuthConfirmation(it)
            }
        }
    }

    private fun onTotalUnreadEmails(resultData: GeneralResult.TotalUnreadEmails){
        when (resultData) {
            is GeneralResult.TotalUnreadEmails.Success -> {
                scene.setToolbarNumberOfEmails(resultData.activeAccountTotal)
                scene.setMenuAccounts(model.extraAccounts, resultData.extraAccountsData.map { it.second })
                resultData.extraAccountsData.forEach {
                    if(it.second > 0) {
                        model.extraAccountHasUnreads = true
                        if(!model.isInMultiSelect)
                            scene.showExtraAccountsBadge(true)
                    }
                }
                scene.updateBadges(resultData.extraAccountsData)

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

    private fun onChangeToNextAccount(resultData: GeneralResult.ChangeToNextAccount){
        when(resultData) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activateAccount(resultData.activeAccount)
                scene.dismissAccountSuspendedDialog()
            }
        }
    }

    private fun onGetRemoteFile(result: GeneralResult.GetRemoteFile) {
        when (result) {
            is GeneralResult.GetRemoteFile.Success -> {
                scene.dismissPreparingFileDialog()
                val file = result.remoteFiles.first()
                host.exitToScene(RestoreBackupParams(true, Pair(file.first, false)), null, false, true)
            }
        }
    }


    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.exitToScene(LinkingParams(resultData.linkAccount,
                        resultData.deviceId, resultData.uuid, resultData.deviceType), null,
                        false, true)
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showToastMessage(resultData.message)
            }
        }
    }

    private fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.exitToScene(LinkingParams(resultData.syncAccount, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), ActivityMessage.SyncMailbox(),
                        false, true)
            }
            is GeneralResult.SyncAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onBackgroundAccountsMailboxUpdated(resultData: GeneralResult.BackgroundAccountsUpdateMailbox) {
        when (resultData) {
            is GeneralResult.BackgroundAccountsUpdateMailbox.Success -> {
                if(resultData.shouldUpdateUI) {
                    handleSyncEvents(resultData.syncEventsList)
                    generalDataSource.submitRequest(GeneralRequest.TotalUnreadEmails(model.selectedLabel.text))
                    dataSource.submitRequest(MailboxRequest.GetMenuInformation())
                    dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
                }
            }
            is GeneralResult.BackgroundAccountsUpdateMailbox.Failure -> dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
        }
        scene.setEmtpyMailboxBackground(model.selectedLabel)
    }

    private fun onActiveAccountUpdateMailbox(resultData: GeneralResult.ActiveAccountUpdateMailbox) {
        scene.clearRefreshing()
        when (resultData) {
            is GeneralResult.ActiveAccountUpdateMailbox.Success -> {
                handleActiveAccountSuccessfulMailboxUpdate(resultData)
                if(shouldSyncBackground){
                    updateBackgroundAccounts()
                }
                dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
            }
            is GeneralResult.ActiveAccountUpdateMailbox.SuccessAndRepeat -> {
                val success = GeneralResult.ActiveAccountUpdateMailbox.Success(
                        mailboxLabel = resultData.mailboxLabel,
                        isManual = resultData.isManual,
                        shouldNotify = resultData.shouldNotify,
                        data = resultData.data
                )
                handleActiveAccountSuccessfulMailboxUpdate(success)
                dataSourceController.updateMailbox(model.selectedLabel)
            }
            is GeneralResult.ActiveAccountUpdateMailbox.Failure -> {
                scene.showMessage(resultData.message)
                if(shouldSyncBackground){
                    updateBackgroundAccounts()
                }
                dataSource.submitRequest(MailboxRequest.ResendPeerEvents())
            }
            is GeneralResult.ActiveAccountUpdateMailbox.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.Logout(false))
            }
            is GeneralResult.ActiveAccountUpdateMailbox.Forbidden -> {
                scene.showConfirmPasswordDialog(observer)
            }
            is GeneralResult.ActiveAccountUpdateMailbox.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
        scene.setEmtpyMailboxBackground(model.selectedLabel)
    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                if(result.activeAccount == null)
                    host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                            true, true)
                else {
                    activeAccount = result.activeAccount
                    host.exitToScene(MailboxParams(),
                            ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            false, true)
                }

            }
        }
    }

    private fun onLogout(result: GeneralResult.Logout){
        when (result) {
            is GeneralResult.Logout.Success -> {
                CloudBackupJobService.cancelJob(storage, result.oldAccountId)
                if(result.activeAccount == null)
                    host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.expired_session)),
                            true, true)
                else {
                    activeAccount = result.activeAccount
                    host.exitToScene(MailboxParams(),
                            ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            false, true)
                }

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

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(observer, activeAccount.userEmail, dialogType)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissLinkDeviceDialog()
            })
        }

        override fun onSyncDeviceDismiss(accountEmail: String) {
            host.runOnUiThread(Runnable {
                scene.dismissSyncDeviceDialog()
            })
        }

        override fun onAccountSuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    showSuspendedAccountDialog()
            })
        }

        override fun onAccountUnsuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    scene.dismissAccountSuspendedDialog()
            })
        }

        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showSyncDeviceAuthConfirmation(trustedDeviceInfo)
            })
        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {

        }

        override fun onSyncRequestDeny() {

        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onNewEvent(recipientId: String, domain: String) {
            if(recipientId == activeAccount.recipientId && domain == activeAccount.domain) {
                host.runOnUiThread(Runnable {
                    dataSourceController.updateMailbox(model.selectedLabel)
                })
            } else {
                val account = model.extraAccounts.find { it.recipientId == recipientId && it.domain == domain }
                if(account != null){
                    updateBackgroundAccounts()
                }
            }
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
    }

    val shouldSync: Boolean
            get() = System.currentTimeMillis() - model.lastSync > minimumIntervalBetweenSyncs

    val shouldSyncBackground: Boolean
        get() = System.currentTimeMillis() - model.lastSyncBackground > minimumIntervalBetweenBackgroundSyncs


    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            BaseActivity.RequestCode.readAccess.ordinal -> {
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
            BaseActivity.RequestCode.writeAccess.ordinal -> {
                val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.WRITE_EXTERNAL_STORAGE }
                if (indexOfPermission < 0) return
                if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
                    scene.showMessage(UIMessage(R.string.permission_filepicker_rationale))
                    return
                }
                host.launchExternalActivityForResult(ExternalActivityParams.FilePicker())
            }
            else -> return
        }
    }

    companion object {
        val threadsPerPage = 20
        val minimumIntervalBetweenSyncs = 1000L
        val minimumIntervalBetweenBackgroundSyncs = 5000L
        const val TIME_TO_RESEND_EVENTS = 5000L
    }

}
