package com.criptext.mail.scenes.settings.recovery_email

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
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailRequest
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
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

class RecoveryEmailController(
        private val model: RecoveryEmailModel,
        private val scene: RecoveryEmailScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
        private val dataSource: BackgroundWorkManager<RecoveryEmailRequest, RecoveryEmailResult>)
    : SceneController(){

    var lastTimeConfirmationLinkSent: Long
        get() = storage.getLong(KeyValueStorage.StringKey.LastTimeConfirmationLinkSent, 0L)
        set(value) {
            storage.putLong(KeyValueStorage.StringKey.LastTimeConfirmationLinkSent, value)
        }

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.ResetPassword -> onResetPassword(result)
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
        }
    }

    private val recoveryEmailUIObserver = object: RecoveryEmailUIObserver{
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

        override fun onForgotPasswordPressed() {
            generalDataSource.submitRequest(GeneralRequest.ResetPassword(activeAccount.recipientId))
        }

        override fun onRecoveryEmailTextChanged(text: String) {
            val newRecoveryEmail = if (text.isEmpty()) {
                TextInput(value = text, state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateRecoveryEmailAddress(text)
                when (userInput) {
                    is FormData.Valid -> {
                        TextInput(value = userInput.value,
                                state = FormInputState.Valid())
                    }

                    is FormData.Error -> {
                        TextInput(value = text,
                                state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.newRecoveryEmail = newRecoveryEmail
            scene.setRecoveryEmailState(newRecoveryEmail.state)
            toggleChangeEmailButton()
        }

        override fun onChangeButtonPressed(text: String) {
            scene.showEnterPasswordDialog()
        }

        override fun onEnterPasswordOkPressed(password: String) {
            scene.dialogToggleLoad(true)
            dataSource.submitRequest(RecoveryEmailRequest.ChangeRecoveryEmail(password, model.newRecoveryEmail.value))
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
        }

        override fun onResendRecoveryLinkPressed() {
            lastTimeConfirmationLinkSent = System.currentTimeMillis()
            scene.onResendLinkTimeSet(RESEND_TIME)
            dataSource.submitRequest(RecoveryEmailRequest.ResendConfirmationLink())
        }
    }

    private fun toggleChangeEmailButton() {
        if(model.newRecoveryEmail.state is FormInputState.Valid
                && model.newRecoveryEmail.value != model.recoveryEmail) {
            scene.enableChangeButton()
        } else {
            scene.disableChangeButton()
        }

    }

    private val dataSourceListener = { result: RecoveryEmailResult ->
        when (result) {
            is RecoveryEmailResult.ResendConfirmationLink -> onResendConfirmationEmail(result)
            is RecoveryEmailResult.ChangeRecoveryEmail -> onChangeRecoveryEmail(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        model.lastTimeConfirmationLinkSent = lastTimeConfirmationLinkSent
        scene.attachView(recoveryEmailUIObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return false
    }

    private fun onResendConfirmationEmail(result: RecoveryEmailResult.ResendConfirmationLink){
        when(result) {
            is RecoveryEmailResult.ResendConfirmationLink.Success -> {
                model.lastTimeConfirmationLinkSent = lastTimeConfirmationLinkSent
                scene.showConfirmationSentDialog()
            }
            is RecoveryEmailResult.ResendConfirmationLink.Failure -> {
                scene.onResendLinkFailed()
                scene.showMessage(UIMessage(R.string.recovery_confirmation_resend_failed))
            }
        }
    }

    private fun onChangeRecoveryEmail(result: RecoveryEmailResult.ChangeRecoveryEmail){
        when(result) {
            is RecoveryEmailResult.ChangeRecoveryEmail.Success -> {
                model.recoveryEmail = model.newRecoveryEmail.value
                model.isEmailConfirmed = false
                scene.updateCurrent(model)
                scene.enterPasswordDialogDismiss()
                scene.showMessage(UIMessage(R.string.recovery_email_has_changed))
            }
            is RecoveryEmailResult.ChangeRecoveryEmail.Failure -> {
                scene.enterPasswordDialogError(result.message)
                scene.dialogToggleLoad(false)
            }
        }
    }

    private fun onResetPassword(result: GeneralResult.ResetPassword){
        when(result) {
            is GeneralResult.ResetPassword.Success -> {
                scene.showForgotPasswordDialog(result.email)
            }
            is GeneralResult.ResetPassword.Failure -> {
                scene.showMessage(result.message)
            }
        }
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
                scene.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.exitToScene(LinkingParams(activeAccount.userEmail, resultData.deviceId,
                        resultData.uuid), null,
                        false, true)
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
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

        override fun onDeviceLinkAuthAccept(deviceId: Int, name: String) {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onNewEvent() {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {
            model.recoveryEmail = model.newRecoveryEmail.value
            model.isEmailConfirmed = false
            scene.updateCurrent(model)
        }

        override fun onRecoveryEmailConfirmed() {
            model.isEmailConfirmed = true
            scene.updateCurrent(model)
        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(recoveryEmailUIObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
    }

    override fun onBackPressed(): Boolean {
        recoveryEmailUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        val RESEND_TIME = 300000L
    }
}