package com.criptext.mail.scenes.settings.pinlock

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.github.omadahealth.lollipin.lib.managers.LockManager


class PinLockController(
        private val activeAccount: ActiveAccount,
        private val model: PinLockModel,
        private val scene: PinLockScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
        }
    }

    private val uiObserver = object: PinLockUIObserver{
        override fun onAutoTimeSelected(position: Int) {
            if(model.pinActive) {
                storage.putInt(KeyValueStorage.StringKey.PINTimeout, position)
                val lockManager = LockManager.getInstance()
                if(lockManager.appLock != null) {
                    lockManager.appLock.timeout = when (position) {
                        0 -> 500
                        1 -> 60000
                        2 -> 5 * 60000
                        3 -> 15 * 60000
                        4 -> 60 * 60000
                        else -> 24 * 60 * 60000
                    }
                }
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
            host.launchExternalActivityForResult(ExternalActivityParams.PinScreen(true))
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
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

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.ActivatePin) {
            if(activityMessage.isSuccess) {
                scene.setupPINLock()
                uiObserver.onAutoTimeSelected(storage.getInt(KeyValueStorage.StringKey.PINTimeout, 1))
                storage.putBool(KeyValueStorage.StringKey.HasLockPinActive, true)
            }else{
                scene.setPinLockStatus(false)
                scene.togglePinOptions(false)
            }

            return true
        }
        return false
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

    private fun onPasswordChangedRemotely(result: GeneralResult.ConfirmPassword){
        when (result) {
            is GeneralResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is GeneralResult.ConfirmPassword.Failure -> {
                scene.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(),
                        ActivityMessage.ShowUIMessage(
                                UIMessage(R.string.device_removed_remotely_exception)),
                        true, true)
            }
        }
    }

    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onNewEvent() {

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