package com.criptext.mail.scenes.linking

import android.os.Handler
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.linking.data.LinkingRequest
import com.criptext.mail.scenes.linking.data.LinkingResult
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher

class LinkingController(
        private val model: LinkingModel,
        private val scene: LinkingScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
        private val dataSource: BackgroundWorkManager<LinkingRequest, LinkingResult>)
    : SceneController(){

    

    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DataFileCreation -> onDataFileCreation(result)
            is GeneralResult.PostUserData -> onPostUserData(result)
        }
    }

    private val dataSourceListener: (LinkingResult) -> Unit = { result ->
        when(result) {
            is LinkingResult.CheckForKeyBundle -> onCheckForKeyBundle(result)
        }
    }

    private val linkingUIObserver = object: LinkingUIObserver{
        override fun onRetrySyncOk(result: GeneralResult) {
            when(result){
                is GeneralResult.PostUserData -> {
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle))
                }
                is GeneralResult.DataFileCreation -> {
                    generalDataSource.submitRequest(GeneralRequest.DataFileCreation())
                }
            }
        }

        override fun onRetrySyncCancel() {
            host.exitToScene(MailboxParams(), null, false, true)
        }

        override fun onKeepWaitingOk() {
            model.retryTimesCheckForKeyBundle = 0
            delayPostCheckForKeyBundle()
        }

        override fun onKeepWaitingCancel() {
            host.exitToScene(MailboxParams(), null, false, true)
        }

        override fun onCancelSync() {
            host.exitToScene(MailboxParams(), null, false, true)
        }

        override fun onLinkingHasFinished() {
            host.exitToScene(MailboxParams(), null, false, true)
        }

        override fun onBackButtonPressed() {

        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo) {

        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo) {

        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(model = model, linkingUIObserver = linkingUIObserver)
        scene.setProgress(UIMessage(R.string.preparing_mailbox), PREPARING_MAILBOX_PERCENTAGE)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        generalDataSource.submitRequest(GeneralRequest.DataFileCreation())
        return false
    }

    private val webSocketEventListener = object : WebSocketEventListener {
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
                        scene.setProgress(UIMessage(R.string.uploading_mailbox), UPLOADING_MAILBOX_PERCENTAGE)
                        generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                                model.dataFilePath, model.dataFileKey!!, model.randomId, null))
                    }
                }
            })
        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onNewEvent() {

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
                scene.setProgress(UIMessage(R.string.mailbox_upload_successful), SYNC_COMPLETE_PERCENTAGE)
                scene.startSucceedAnimation {
                    linkingUIObserver.onLinkingHasFinished()
                }
            }
            is GeneralResult.PostUserData.Failure -> {
                scene.showRetrySyncDialog(result)
            }
        }
    }

    private fun onDataFileCreation(resultData: GeneralResult.DataFileCreation){
        when (resultData) {
            is GeneralResult.DataFileCreation.Success -> {
                scene.setProgress(UIMessage(R.string.getting_keys), GETTING_KEYS_PERCENTAGE)
                model.dataFileHasBeenCreated = true
                model.dataFilePath = resultData.filePath
                model.dataFileKey = resultData.key
                if(model.untrustedDevicePostedKeyBundle){
                    scene.setProgress(UIMessage(R.string.uploading_mailbox), UPLOADING_MAILBOX_PERCENTAGE)
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle))
                }else{
                    delayPostCheckForKeyBundle()
                }
            }
            is GeneralResult.DataFileCreation.Progress -> {
                scene.setProgress(resultData.message, resultData.progress)
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
                    scene.setProgress(UIMessage(R.string.uploading_mailbox), UPLOADING_MAILBOX_PERCENTAGE)
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle))
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
        val handler = Handler()
            handler.postDelayed(Runnable {
                if(model.retryTimesCheckForKeyBundle < RETRY_TIMES_DEFAULT) {
                    if (!model.untrustedDevicePostedKeyBundle)
                        dataSource.submitRequest(LinkingRequest.CheckForKeyBundle(model.remoteDeviceId))
                    model.retryTimesCheckForKeyBundle++
                }else{
                    scene.showKeepWaitingDialog()
                }
            }, RETRY_TIME)
    }


    override fun onStop() {
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
        const val RETRY_TIME = 5000L
        const val RETRY_TIMES_DEFAULT = 12

        //Sync Process Percentages
        const val  PREPARING_MAILBOX_PERCENTAGE = 40
        const val  GETTING_KEYS_PERCENTAGE = 60
        const val  UPLOADING_MAILBOX_PERCENTAGE = 90
        const val  SYNC_COMPLETE_PERCENTAGE = 100
    }
}