package com.criptext.mail.scenes.settings.changepassword

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.api.models.TrustedDeviceInfo
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.changepassword.data.ChangePasswordRequest
import com.criptext.mail.scenes.settings.changepassword.data.ChangePasswordResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher

class ChangePasswordController(
        private val activeAccount: ActiveAccount,
        private val model: ChangePasswordModel,
        private val scene: ChangePasswordScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
        private val dataSource: BackgroundWorkManager<ChangePasswordRequest, ChangePasswordResult>)
    : SceneController(){

    val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.ResetPassword -> onResetPassword(result)
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
        }
    }

    private val changePasswordUIObserver = object: ChangePasswordUIObserver{

        override fun onSnackbarClicked() {

        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: TrustedDeviceInfo) {
            if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: TrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {

        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo) {
            if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.LinkDenied(untrustedDeviceInfo))
        }

        override fun onForgotPasswordPressed() {
            generalDataSource.submitRequest(GeneralRequest.ResetPassword(activeAccount.recipientId))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
        }

        override fun onOldPasswordChangedListener(password: String) {
            model.oldPasswordText = password
            if(model.oldPasswordText != model.lastUsedPassword)
                scene.showOldPasswordError(null)
            else
                scene.showOldPasswordError(UIMessage(R.string.password_enter_error))
        }

        override fun onPasswordChangedListener(password: String) {
            model.passwordText = password
            if(model.confirmPasswordText.isNotEmpty())
                checkPasswords(Pair(model.passwordText, model.confirmPasswordText))
        }

        override fun onConfirmPasswordChangedListener(confirmPassword: String) {
            model.confirmPasswordText = confirmPassword
            checkPasswords(Pair(model.confirmPasswordText, model.passwordText))
        }

        override fun onChangePasswordButtonPressed() {
            keyboardManager.hideKeyboard()
            model.lastUsedPassword = model.oldPasswordText
            dataSource.submitRequest(ChangePasswordRequest.ChangePassword(model.oldPasswordText, model.confirmPasswordText))
        }
    }

    private val dataSourceListener = { result: ChangePasswordResult ->
        when (result) {
            is ChangePasswordResult.ChangePassword -> onChangePasswordResult(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(changePasswordUIObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return false
    }

    private fun checkPasswords(passwords: Pair<String, String>) {
        if (arePasswordsMatching && passwords.first.length >= minimumPasswordLength) {
            scene.showPasswordDialogError(null)
            model.passwordState = FormInputState.Valid()
            if (model.passwordState is FormInputState.Valid)
                scene.toggleChangePasswordButton(true)
        } else if (arePasswordsMatching && passwords.first.isEmpty()) {
            scene.showPasswordDialogError(null)
            model.passwordState = FormInputState.Unknown()
            scene.toggleChangePasswordButton(false)
        } else if (arePasswordsMatching && passwords.first.length < minimumPasswordLength) {
            val errorMessage = UIMessage(R.string.password_length_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.showPasswordDialogError(errorMessage)
            scene.toggleChangePasswordButton(false)
        } else {
            val errorMessage = UIMessage(R.string.password_mismatch_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.showPasswordDialogError(errorMessage)
            scene.toggleChangePasswordButton(false)
        }
    }

    private fun onChangePasswordResult(result: ChangePasswordResult.ChangePassword){
        when(result) {
            is ChangePasswordResult.ChangePassword.Success -> {
                scene.showMessage(UIMessage(R.string.change_password_success))
                host.finishScene()
            }
            is ChangePasswordResult.ChangePassword.Failure -> {
                scene.showOldPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onResetPassword(result: GeneralResult.ResetPassword){
        when(result) {
            is GeneralResult.ResetPassword.Success -> {
                scene.showForgotPasswordDialog(result.email)
            }
            is GeneralResult.ResetPassword.Failure -> {
                scene.showForgotPasswordDialog(null)
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

    private fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.exitToScene(LinkingParams(activeAccount.userEmail, resultData.deviceId,
                        resultData.uuid, resultData.deviceType), ActivityMessage.SyncMailbox(),
                        false, true)
            }
            is GeneralResult.SyncAccept.Failure -> {
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
                scene.setConfirmPasswordError(result.message)
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
        override fun onSyncBeginRequest(trustedDeviceInfo: TrustedDeviceInfo) {

        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {

        }

        override fun onSyncRequestDeny() {

        }

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
                scene.showConfirmPasswordDialog(changePasswordUIObserver)
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
        changePasswordUIObserver.onBackButtonPressed()
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