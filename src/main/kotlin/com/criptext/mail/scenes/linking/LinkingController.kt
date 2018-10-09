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
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailRequest
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
import com.criptext.mail.scenes.signin.SignInSceneController
import com.criptext.mail.scenes.signin.data.LinkDeviceState
import com.criptext.mail.scenes.signin.data.SignInRequest
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput
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
            if(!model.untrustedDevicePostedKeyBundle) {
                model.remoteDeviceId = deviceId
                model.untrustedDevicePostedKeyBundle = true
                if (model.dataFileHasBeenCreated) {
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!, model.randomId, null))
                }
            }
        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {

        }

        override fun onDeviceLinkAuthAccept(deviceId: Int, name: String) {

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
                scene.startSucceedAnimation {
                    linkingUIObserver.onLinkingHasFinished()
                }
            }
            is GeneralResult.PostUserData.Failure -> {
                scene.showStatusMessage(UIMessage(R.string.server_error_exception))
                host.exitToScene(MailboxParams(), null,false, true)
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
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle))
                }else{
                    dataSource.submitRequest(LinkingRequest.CheckForKeyBundle(model.remoteDeviceId))
                }
            }
            is GeneralResult.DataFileCreation.Failure -> {
                scene.showStatusMessage(UIMessage(R.string.server_error_exception))
                host.exitToScene(MailboxParams(), null,false, true)
            }
        }
    }

    private fun onCheckForKeyBundle(result: LinkingResult.CheckForKeyBundle){
        when (result) {
            is LinkingResult.CheckForKeyBundle.Success -> {
                if(!model.untrustedDevicePostedKeyBundle) {
                    model.untrustedDevicePostedKeyBundle = true
                    model.keyBundle = result.keyBundle
                    generalDataSource.submitRequest(GeneralRequest.PostUserData(model.remoteDeviceId,
                            model.dataFilePath, model.dataFileKey!!,model.randomId, model.keyBundle))
                }
            }
            is LinkingResult.CheckForKeyBundle.Failure -> {
                val handler = Handler()
                host.runOnUiThread(Runnable {
                    handler.postDelayed(Runnable {
                        if(!model.untrustedDevicePostedKeyBundle)
                            dataSource.submitRequest(LinkingRequest.CheckForKeyBundle(model.remoteDeviceId))
                    }, RETRY_TIME)
                })
            }
        }
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
    }
}