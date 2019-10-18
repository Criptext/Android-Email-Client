package com.criptext.mail.scenes.settings.syncing

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
    : SceneController(){



    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.LinkDataReady -> onLinkDataReady(result)
            is GeneralResult.LinkData -> onLinkData(result)
        }
    }

    private val linkingUIObserver = object: SyncingUIObserver{
        override fun onResendDeviceLinkAuth(username: String) {

        }

        override fun onBackPressed() {

        }

        override fun onRetrySyncOk(result: GeneralResult) {
            when(result){
                is GeneralResult.LinkData -> {
                    generalDataSource.submitRequest(GeneralRequest.LinkData(model.key, model.dataAddress, model.remoteDeviceId))
                }
            }
        }

        override fun onRetrySyncCancel() {
            host.exitToScene(MailboxParams(), null, false, true)
        }


        override fun onLinkingHasFinished() {
            host.exitToScene(MailboxParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.mailbox_sync_complete)), false, true)
        }

        override fun onBackButtonPressed() {

        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        PinLockUtils.enablePinLock()

        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(model = model, syncingUIObserver = linkingUIObserver)
        scene.setProgress(UIMessage(R.string.waiting_for_mailbox), WAITING_FOR_MAILBOX_PERCENTAGE)
        generalDataSource.listener = generalDataSourceListener
        generalDataSource.submitRequest(GeneralRequest.LinkDataReady())
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
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

        }

        override fun onSyncRequestDeny() {

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
                scene.setProgress(UIMessage(R.string.sync_complete), SYNC_COMPLETE_PERCENTAGE)
                scene.startSucceedAnimation {
                    linkingUIObserver.onLinkingHasFinished()
                }
            }
            is GeneralResult.LinkData.Progress -> {
                scene.setProgress(result.message, result.progress)
            }
            is GeneralResult.LinkData.Failure -> {
                scene.showRetrySyncDialog(result)
            }
        }
    }


    override fun onPause() {
        cleanup()
    }

    override fun onStop() {
        cleanup()
    }

    private fun cleanup(){
        websocketEvents.clearListener(webSocketEventListener)
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