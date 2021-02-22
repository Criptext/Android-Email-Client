package com.criptext.mail.scenes.linking

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.linking.data.LinkingDataSource
import com.criptext.mail.scenes.linking.data.LinkingRequest
import com.criptext.mail.scenes.linking.data.LinkingResult
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.*
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher

class LinkingController(
        private val model: LinkingModel,
        private val scene: LinkingScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: LinkingDataSource)
    : SceneController(host, activeAccount, storage){

    

    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DataFileCreation -> onDataFileCreation(result)
            is GeneralResult.PostUserData -> onPostUserData(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val dataSourceListener: (LinkingResult) -> Unit = { result ->
        when(result) {
            is LinkingResult.CheckForKeyBundle -> onCheckForKeyBundle(result)
        }
    }

    private val linkingUIObserver = object: LinkingUIObserver(generalDataSource, host) {
        override fun onSnackbarClicked() {

        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn -> {
                            host.goToScene(SignInParams(true), true)
                        }
                    }
                }
            }
        }

        override fun onRetrySyncOk(result: GeneralResult) {
            when(result){
                is GeneralResult.PostUserData -> {
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle,
                            model.incomingAccount))
                }
                is GeneralResult.DataFileCreation -> {
                    generalDataSource.submitRequest(
                            GeneralRequest.DataFileCreation(model.incomingAccount.recipientId,
                                    model.incomingAccount.domain)
                    )
                }
            }
        }

        override fun onRetrySyncCancel() {
            exitToMailbox()
        }

        override fun onKeepWaitingOk() {
            model.retryTimesCheckForKeyBundle = 0
            delayPostCheckForKeyBundle()
        }

        override fun onKeepWaitingCancel() {
            exitToMailbox()
        }

        override fun onCancelSync() {
            exitToMailbox()
        }

        override fun onLinkingHasFinished() {
            exitToMailbox()
        }

        override fun onBackButtonPressed() {

        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        private fun exitToMailbox(){
            host.stopMessagesAndCallbacks()
            host.goToScene(params = MailboxParams(), activityMessage = null,
                    keep = false, deletePastIntents = true)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(model = model, linkingUIObserver = linkingUIObserver)
        scene.setProgressStatus(
                UIMessage(R.string.preparing_mailbox),
                animationData = GeneralAnimationData(
                        start = ExportMailboxAnimationData.encryptingLoop.first,
                        end = ExportMailboxAnimationData.encryptingLoop.second,
                        isLoop = true
                )
        )
        scene.setProgress(PREPARING_MAILBOX_PERCENTAGE)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        if(activityMessage != null && activityMessage is ActivityMessage.SyncMailbox)
            model.untrustedDevicePostedKeyBundle = true
        generalDataSource.submitRequest(
                GeneralRequest.DataFileCreation(model.incomingAccount.recipientId,
                        model.incomingAccount.domain)
        )
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    override fun onPause() {
        cleanup()
    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    private fun cleanup(){
        host.stopMessagesAndCallbacks()
        websocketEvents.clearListener(webSocketEventListener)
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(linkingUIObserver, activeAccount.userEmail, dialogType)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {
            host.goToScene(params = MailboxParams(), activityMessage = null,
                    keep = false, deletePastIntents = true)
        }

        override fun onSyncDeviceDismiss(accountEmail: String) {
            host.goToScene(params = MailboxParams(), activityMessage = null,
                    keep = false, deletePastIntents = true)
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
            host.runOnUiThread(Runnable {
                if (!model.untrustedDevicePostedKeyBundle) {
                    model.remoteDeviceId = deviceId
                    model.untrustedDevicePostedKeyBundle = true
                    if (model.dataFileHasBeenCreated) {
                        scene.setProgressStatus(UIMessage(R.string.uploading_mailbox),
                                animationData = GeneralAnimationData(
                                        start = ExportMailboxAnimationData.exportingTransition.first,
                                        end = ExportMailboxAnimationData.exportingTransition.second,
                                        isLoop = true
                                )
                        )
                        scene.setProgress(UPLOADING_MAILBOX_PERCENTAGE, onFinish = {
                            generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                                    model.dataFilePath, model.dataFileKey!!, model.randomId, null,
                                    model.incomingAccount))
                        })

                    }
                }
            })
        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onNewEvent(recipientId: String, domain: String) {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {

        }

        override fun onDeviceRemoved() {

        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

    private fun onPostUserData(result: GeneralResult.PostUserData){
        when (result) {
            is GeneralResult.PostUserData.Success -> {
                scene.setProgress(SYNC_COMPLETE_PERCENTAGE)
                scene.setProgressStatus(UIMessage(R.string.mailbox_upload_successful),
                        animationData = GeneralAnimationData(
                                start = ExportMailboxAnimationData.exportingTransition.first,
                                end = ExportMailboxAnimationData.exportingTransition.second,
                                isLoop = false
                        ),
                        onFinish = {
                            scene.showCompleteExport()
                            host.postDelay(Runnable {
                                linkingUIObserver.onLinkingHasFinished()
                            }, 1000L)
                        }
                )
            }
            is GeneralResult.PostUserData.Failure -> {
                scene.showRetrySyncDialog(result)
            }
        }
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                generalDataSource.signalClient = SignalClient.Default(SignalStoreCriptext(generalDataSource.db, activeAccount))
                dataSource.activeAccount = activeAccount
                scene.dismissAccountSuspendedDialog()

                host.goToScene(params = MailboxParams(), activityMessage = null,
                        keep = false, deletePastIntents = true)
            }
        }
    }

    private fun onDataFileCreation(resultData: GeneralResult.DataFileCreation){
        when (resultData) {
            is GeneralResult.DataFileCreation.Success -> {
                model.dataFileHasBeenCreated = true
                model.dataFilePath = resultData.filePath
                model.dataFileKey = resultData.key
                if(model.untrustedDevicePostedKeyBundle){
                    scene.setProgressStatus(UIMessage(R.string.getting_keys),
                            animationData = GeneralAnimationData(
                                    start = ExportMailboxAnimationData.receivingKeysLoop.first,
                                    end = ExportMailboxAnimationData.receivingKeysLoop.second,
                                    isLoop = true
                            )
                    )
                    host.postDelay(Runnable {
                        scene.setProgress(UPLOADING_MAILBOX_PERCENTAGE)
                        scene.setProgressStatus(
                                message = UIMessage(R.string.uploading_mailbox),
                                animationData = GeneralAnimationData(
                                        start = ExportMailboxAnimationData.receivingKeysToExportTransition.first,
                                        end = ExportMailboxAnimationData.receivingKeysToExportTransition.second,
                                        isLoop = false
                                ),
                                onFinish = {
                                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                                            model.dataFilePath, model.dataFileKey!!, model.randomId, model.keyBundle,
                                            model.incomingAccount))
                                })
                    }, 2000L)
                }else{
                    delayPostCheckForKeyBundle()
                }
            }
            is GeneralResult.DataFileCreation.Progress -> {
                scene.setProgressStatus(resultData.message, null)
                scene.setProgress(resultData.progress)
            }
            is GeneralResult.DataFileCreation.Failure -> {
                scene.showRetrySyncDialog(resultData)
            }
        }
    }

    private fun onCheckForKeyBundle(result: LinkingResult.CheckForKeyBundle){
        when (result) {
            is LinkingResult.CheckForKeyBundle.Success -> {
                if(!model.untrustedDevicePostedKeyBundle) {
                    model.untrustedDevicePostedKeyBundle = true
                    model.keyBundle = result.keyBundle
                    scene.setProgressStatus(
                            message = UIMessage(R.string.uploading_mailbox),
                            animationData = GeneralAnimationData(
                                start = ExportMailboxAnimationData.receivingKeysToExportTransition.first,
                                end = ExportMailboxAnimationData.receivingKeysToExportTransition.second,
                                isLoop = false
                            ),
                            onFinish = {
                                generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                                        model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle,
                                        model.incomingAccount))
                            })
                    scene.setProgress(UPLOADING_MAILBOX_PERCENTAGE)
                }
            }
            is LinkingResult.CheckForKeyBundle.Failure -> {
                host.runOnUiThread(Runnable {
                    delayPostCheckForKeyBundle()
                })
            }
        }
    }

    private fun delayPostCheckForKeyBundle(){
        scene.setProgressStatus(
                message = UIMessage(R.string.uploading_mailbox),
                animationData = GeneralAnimationData(
                        start = ExportMailboxAnimationData.receivingKeysLoop.first,
                        end = ExportMailboxAnimationData.receivingKeysLoop.second,
                        isLoop = true
                )
        )
        host.postDelay(Runnable {
            if(model.retryTimesCheckForKeyBundle < RETRY_TIMES_DEFAULT) {
                if (!model.untrustedDevicePostedKeyBundle) {
                    dataSource.submitRequest(LinkingRequest.CheckForKeyBundle(model.incomingAccount, model.remoteDeviceId))
                }
                model.retryTimesCheckForKeyBundle++
            }else{
                scene.showKeepWaitingDialog()
            }
        }, RETRY_TIME)
    }


    override fun onStop() {
        cleanup()
    }

    override fun onBackPressed(): Boolean {
        linkingUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val RETRY_TIME = 5000L
        const val RETRY_TIMES_DEFAULT = 12

        //Sync Process Percentages
        const val  PREPARING_MAILBOX_PERCENTAGE = 40
        const val  GETTING_KEYS_PERCENTAGE = 60
        const val  UPLOADING_MAILBOX_PERCENTAGE = 90
        const val  SYNC_COMPLETE_PERCENTAGE = 100
    }
}