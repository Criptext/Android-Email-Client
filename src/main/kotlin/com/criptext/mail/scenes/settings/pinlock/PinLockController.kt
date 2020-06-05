package com.criptext.mail.scenes.settings.pinlock

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PinLockUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.github.omadahealth.lollipin.lib.managers.LockManager


class PinLockController(
        private var activeAccount: ActiveAccount,
        private val model: PinLockModel,
        private val scene: PinLockScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource)
    : SceneController(host, activeAccount, storage){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
        }
    }

    private val uiObserver = object: PinLockUIObserver(generalDataSource, host){
        override fun onSnackbarClicked() {

        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {

        }

        override fun onAutoTimeSelected(position: Int) {
            if(model.pinActive) {
                storage.putInt(KeyValueStorage.StringKey.PINTimeout, position)
                PinLockUtils.setPinLockTimeoutPosition(position)
            }

        }

        override fun onPinSwitchChanged(isEnabled: Boolean) {
            scene.togglePinOptions(isEnabled)
            val lockManager = LockManager.getInstance()
            if(isEnabled){
                if(!storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)){
                    if(lockManager.appLock == null || !lockManager.appLock.isPasscodeSet) {
                        host.launchExternalActivityForResult(ExternalActivityParams.PinScreen(true))
                    }else {
                        storage.putBool(KeyValueStorage.StringKey.HasLockPinActive, true)
                    }
                }
            }else{
                lockManager.appLock?.disableAndRemoveConfiguration()
                storage.putBool(KeyValueStorage.StringKey.HasLockPinActive, false)
            }
        }

        override fun onPinChangePressed() {
            host.launchExternalActivityForResult(ExternalActivityParams.PinScreen(false))
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.goToScene(
                    params = SettingsParams(),
                    activityMessage = null,
                    animationData = TransitionAnimationData(
                            forceAnimation = true,
                            enterAnim = 0,
                            exitAnim = R.anim.slide_out_right
                    ),
                    keep = false
            )
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)

        model.pinTimeOut = storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1)
        model.pinActive = storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false)

        scene.attachView(uiObserver, keyboardManager, model)
        generalDataSource.listener = generalDataSourceListener

        val handleMessage = handleActivityMessage(activityMessage)

        scene.setPinLockStatus(storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false))
        scene.togglePinOptions(storage.getBool(KeyValueStorage.StringKey.HasLockPinActive, false))


        return handleMessage
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.ActivatePin) {
            if(activityMessage.isSuccess) {
                scene.setupPINLock()
                model.pinActive = true
                uiObserver.onAutoTimeSelected(storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1))
                storage.putBool(KeyValueStorage.StringKey.HasLockPinActive, model.pinActive)
            }else{
                scene.setPinLockStatus(false)
                scene.togglePinOptions(false)
            }

            return true
        }
        return false
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

        }

        override fun onAccountUnsuspended(accountEmail: String) {

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

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onNewEvent(recipientId: String, domain: String) {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(uiObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
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
        const val minimumPasswordLength = 8
    }
}