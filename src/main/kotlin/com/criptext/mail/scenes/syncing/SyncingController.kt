package com.criptext.mail.scenes.syncing

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.scenes.syncing.holders.SyncingLayoutState
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher

class SyncingController(
        private val model: SyncingModel,
        private val scene: SyncingScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>)
    : SceneController(host, activeAccount, storage){



    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.LinkDataReady -> onLinkDataReady(result)
            is GeneralResult.LinkData -> onLinkData(result)
            is GeneralResult.SyncStatus -> onSyncStatus(result)
            is GeneralResult.SyncBegin -> onSyncBegin(result)
        }
    }

    private val uiObserver = object: SyncingUIObserver{
        override fun onSubmitButtonPressed() {
            when(model.state){
                is SyncingLayoutState.SyncBegin -> {
                    generalDataSource.submitRequest(GeneralRequest.SyncBegin())
                }
                is SyncingLayoutState.SyncRejected -> {
                    restartSyncProcess()
                }
            }

        }

        override fun onRetrySyncOk(result: GeneralResult) {
            when(result){
                is GeneralResult.LinkData -> {
                    restartSyncProcess()
                }
            }
        }

        override fun onRetrySyncCancel() {
            host.goToScene(
                    params = MailboxParams(),
                    activityMessage = null,
                    keep = false,
                    deletePastIntents = true
            )
        }


        override fun onLinkingHasFinished() {
            host.goToScene(
                    params = MailboxParams(),
                    activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.mailbox_sync_complete)),
                    keep = false,
                    deletePastIntents = true
            )
        }

        override fun onBackButtonPressed() {
            when(model.state){
                is SyncingLayoutState.SyncBegin -> {
                    host.finishScene()
                }
                is SyncingLayoutState.SyncRejected -> {
                    restartSyncProcess()
                }
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(model = model, syncingUIObserver = uiObserver)
        generalDataSource.listener = generalDataSourceListener
        generalDataSource.submitRequest(GeneralRequest.SyncStatus())
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun handleSyncStatusSuccess(syncStatusData: SyncStatusData){
        host.getHandler()?.removeCallbacks(null)
        model.retryTimeLinkStatus = 0
        model.authorizerName = syncStatusData.authorizerName
        model.randomId = syncStatusData.randomId
        model.remoteDeviceId = syncStatusData.authorizerId
        model.deviceType = syncStatusData.authorizerType
        model.state = SyncingLayoutState.SyncImport()
        host.runOnUiThread(Runnable {
            scene.attachView(model, uiObserver)
            scene.setProgressStatus(
                    message = UIMessage(R.string.sending_keys),
                    drawable = R.drawable.img_keysexport
            )
            scene.setProgress(
                    progress = SENDING_KEYS_PERCENTAGE,
                    onFinish = {
                        host.postDelay(Runnable {
                            scene.setProgressStatus(
                                    message = UIMessage(R.string.waiting_for_mailbox),
                                    drawable = R.drawable.img_waitingimport
                            )
                            scene.setProgress(
                                    progress = WAITING_FOR_MAILBOX_PERCENTAGE,
                                    onFinish = {
                                        generalDataSource.submitRequest(GeneralRequest.LinkDataReady())
                                    }
                            )
                        }, 1000L)
                    }
            )
        })
    }

    private fun restartSyncProcess(){
        model.state = SyncingLayoutState.SyncBegin()
        scene.attachView(model, uiObserver)
        generalDataSource.submitRequest(GeneralRequest.SyncBegin())
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {

        }

        override fun onSyncDeviceDismiss(accountEmail: String) {

        }

        override fun onAccountSuspended(accountEmail: String) {

        }

        override fun onAccountUnsuspended(accountEmail: String) {

        }

        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {

        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {
            if(model.state !is SyncingLayoutState.SyncImport) {
                handleSyncStatusSuccess(syncStatusData)
            }
        }

        override fun onSyncRequestDeny() {
            host.runOnUiThread(Runnable {
                model.state = SyncingLayoutState.SyncRejected()
                scene.attachView(model, uiObserver)
            })
        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

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

    private fun onLinkDataReady(result: GeneralResult) {
        when (result) {
            is GeneralResult.LinkDataReady.Success -> {
                model.key = result.key
                model.dataAddress = result.dataAddress
                scene.disableSkip()
                generalDataSource.submitRequest(GeneralRequest.LinkData(model.key, model.dataAddress,
                        model.remoteDeviceId))

            }
            is GeneralResult.LinkDataReady.Failure -> {
                host.postDelay(Runnable{
                    if(model.retryTimeLinkDataReady < RETRY_TIMES_DATA_READY) {
                        generalDataSource.submitRequest(GeneralRequest.LinkDataReady())
                        model.retryTimeLinkDataReady++
                    }
                }, RETRY_TIME_DATA_READY)
            }
        }
    }

    private fun onLinkData(result: GeneralResult) {
        when (result) {
            is GeneralResult.LinkData.Success -> {
                scene.setProgress(
                        progress = SYNC_COMPLETE_PERCENTAGE,
                        onFinish = {
                            scene.setProgressStatus(
                                    message = UIMessage(R.string.device_ready),
                                    drawable = R.drawable.img_readyimport
                            )
                            host.postDelay(Runnable{
                                uiObserver.onLinkingHasFinished()
                            }, 1000L)
                        }
                )
            }
            is GeneralResult.LinkData.Progress -> {
                scene.setProgressStatus(
                        message = result.message,
                        drawable = result.drawable
                )
                scene.setProgress(
                        progress = result.progress,
                        onFinish = null
                )
            }
            is GeneralResult.LinkData.Failure -> {
                scene.showRetrySyncDialog(result)
            }
        }
    }

    private fun onSyncBegin(result: GeneralResult.SyncBegin){
        when(result) {
            is GeneralResult.SyncBegin.Success -> {
                model.state = SyncingLayoutState.SyncImport()
                scene.attachView(model, uiObserver)
                generalDataSource.submitRequest(GeneralRequest.SyncStatus())
            }
            is GeneralResult.SyncBegin.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private fun onSyncStatus(result: GeneralResult.SyncStatus) {
        when (result) {
            is GeneralResult.SyncStatus.Success -> {
                if(model.state !is SyncingLayoutState.SyncImport) {
                    handleSyncStatusSuccess(result.syncStatusData)
                }
            }
            is GeneralResult.SyncStatus.Waiting -> {
                host.postDelay(Runnable {
                    if (model.retryTimeLinkStatus < RETRY_TIMES_DEFAULT) {
                        generalDataSource.submitRequest(GeneralRequest.SyncStatus())
                        model.retryTimeLinkStatus++
                    }
                }, RETRY_TIME_DEFAULT)
            }
            is GeneralResult.SyncStatus.Denied -> {
                model.state = SyncingLayoutState.SyncRejected()
                scene.attachView(model, uiObserver)
                model.retryTimeLinkStatus = 0
            }
        }
    }


    override fun onPause() {
        cleanup()
    }

    override fun onStop() {
        cleanup()
    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    private fun cleanup(){
        websocketEvents.clearListener(webSocketEventListener)
    }

    override fun onBackPressed(): Boolean {
        uiObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val RETRY_TIME_DEFAULT = 5000L
        const val RETRY_TIME_DATA_READY = 10000L
        const val RETRY_TIMES_DEFAULT = 12
        const val RETRY_TIMES_DATA_READY = 18

        //Sync Process Percentages
        const val  SENDING_KEYS_PERCENTAGE = 10
        const val  WAITING_FOR_MAILBOX_PERCENTAGE = 40
        const val  DOWNLOADING_MAILBOX_PERCENTAGE = 70
        const val  SYNC_COMPLETE_PERCENTAGE = 100
    }
}