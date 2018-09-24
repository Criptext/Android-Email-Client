package com.criptext.mail.scenes.settings

import android.content.Intent
import com.criptext.mail.BaseActivity
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.params.ChangePasswordParams
import com.criptext.mail.scenes.params.RecoveryEmailParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.params.SignatureParams
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.scenes.settings.devices.DeviceWrapperListController
import com.criptext.mail.scenes.settings.labels.LabelWrapperListController
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.websocket.WebSocketController
import com.criptext.mail.websocket.WebSocketEventListener

class SettingsController(
        private val model: SettingsModel,
        private val scene: SettingsScene,
        private val host: IHostActivity,
        private val websocketEvents: WebSocketController,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
        private val dataSource: BackgroundWorkManager<SettingsRequest, SettingsResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val labelWrapperListController = LabelWrapperListController(model, scene.getLabelListView())
    private val deviceWrapperListController = DeviceWrapperListController(model, scene.getDeviceListView())


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
        }
    }

    private val dataSourceListener = { result: SettingsResult ->
        when (result) {
            is SettingsResult.ChangeContactName -> onContactNameChanged(result)
            is SettingsResult.GetCustomLabels -> onGetCustomLabels(result)
            is SettingsResult.CreateCustomLabel -> onCreateCustomLabels(result)
            is SettingsResult.Logout -> onLogout(result)
            is SettingsResult.GetUserSettings -> onGetUserSettings(result)
            is SettingsResult.RemoveDevice -> onRemoveDevice(result)
            is SettingsResult.ResetPassword -> onResetPassword(result)
        }
    }

    private val settingsUIObserver = object: SettingsUIObserver{
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

        override fun onChangePasswordOptionClicked() {
            host.goToScene(ChangePasswordParams(), false)
        }

        override fun onRecoveryEmailOptionClicked() {
            host.goToScene(RecoveryEmailParams(model.isEmailConfirmed, model.recoveryEmail), false)
        }

        override fun onCustomLabelNameAdded(labelName: String) {
            dataSource.submitRequest(SettingsRequest.CreateCustomLabel(labelName))
            keyboardManager.hideKeyboard()
        }

        override fun onProfileNameClicked() {
            scene.showProfileNameDialog(model.fullName)
        }

        override fun onPrivacyPoliciesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/privacy")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onTermsOfServiceClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/terms")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onOpenSourceLibrariesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/open-source-android")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onLogoutClicked() {
            scene.showLogoutDialog()
        }

        override fun onLogoutConfirmedClicked() {
            scene.showLoginOutDialog()
            dataSource.submitRequest(SettingsRequest.Logout())
        }

        override fun onRemoveDevice(deviceId: Int, position: Int) {
            scene.showRemoveDeviceDialog(deviceId, position)
        }

        override fun onRemoveDeviceConfirmed(deviceId: Int, position: Int, password: String) {
            scene.removeDeviceDialogToggleLoad(true)
            keyboardManager.hideKeyboard()
            dataSource.submitRequest(SettingsRequest.RemoveDevice(deviceId, position, password))
        }

        override fun onRemoveDeviceCancel() {
            keyboardManager.hideKeyboard()
        }

        override fun onBackButtonPressed() {
            host.finishScene()
        }

        override fun onSignatureOptionClicked() {
            host.goToScene(SignatureParams(activeAccount.recipientId), true)
        }

        override fun onToggleLabelSelection(label: LabelWrapper) {
            dataSource.submitRequest(SettingsRequest.ChangeVisibilityLabel(label.id, label.isSelected))
        }

        override fun onCreateLabelClicked() {
            scene.showCreateLabelDialog(keyboardManager)
        }

        override fun onProfileNameChanged(fullName: String) {
            keyboardManager.hideKeyboard()
            activeAccount.updateFullName(storage, fullName)
            model.fullName = fullName
            dataSource.submitRequest(SettingsRequest.ChangeContactName(
                    fullName = fullName,
                    recipientId = activeAccount.recipientId
            ))
        }
    }

    private val onDevicesListItemListener = object: DevicesListItemListener {
        override fun onDeviceTrashClicked(device: DeviceItem, position: Int): Boolean {
            settingsUIObserver.onRemoveDevice(device.id, position)
            return true
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        model.fullName = activeAccount.name
        model.signature = activeAccount.signature
        if(model.labels.isEmpty()) {
            dataSource.submitRequest(SettingsRequest.GetCustomLabels())
        }
        if(model.devices.isEmpty()) {
            model.devices.add(DeviceItem(
                    id = activeAccount.deviceId,
                    friendlyName = DeviceUtils.getDeviceFriendlyName(),
                    name = DeviceUtils.getDeviceName(),
                    isCurrent = true,
                    deviceType = DeviceUtils.getDeviceType().ordinal))
            scene.attachView(
                    name = activeAccount.name,
                    model = model,
                    settingsUIObserver = settingsUIObserver,
                    devicesListItemListener = onDevicesListItemListener)
            dataSource.submitRequest(SettingsRequest.GetUserSettings())
        }
        return false
    }

    override fun onStop() {
        websocketEvents.clearListener(webSocketEventListener)
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    private fun onDeviceRemovedRemotely(result: GeneralResult.DeviceRemoved){
        when (result) {
            is GeneralResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)), true, true)
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

    private fun onContactNameChanged(result: SettingsResult.ChangeContactName){
        when(result) {
            is SettingsResult.ChangeContactName.Success -> {
                scene.showMessage(UIMessage(R.string.profile_name_updated))
            }
            is SettingsResult.ChangeContactName.Failure -> {
                scene.showMessage(UIMessage(R.string.error_updating_account))
            }
        }
    }

    private fun onCreateCustomLabels(result: SettingsResult.CreateCustomLabel){
        when(result) {
            is SettingsResult.CreateCustomLabel.Success -> {
                val labelWrapper = LabelWrapper(result.label)
                labelWrapper.isSelected = true
                labelWrapperListController.addNew(labelWrapper)
            }
            is SettingsResult.CreateCustomLabel.Failure -> {
                scene.showMessage(UIMessage(R.string.error_creating_labels))
                host.finishScene()
            }
        }
    }

    private fun onGetCustomLabels(result: SettingsResult.GetCustomLabels){
        when(result) {
            is SettingsResult.GetCustomLabels.Success -> {
                model.labels.addAll(result.labels.map {
                    val labelWrapper = LabelWrapper(it)
                    labelWrapper.isSelected = it.visible
                    labelWrapper
                })
                labelWrapperListController.notifyDataSetChange()
            }
            is SettingsResult.GetCustomLabels.Failure -> {
                scene.showMessage(UIMessage(R.string.error_getting_labels))
                host.finishScene()
            }
        }
    }

    private fun onLogout(result: SettingsResult.Logout){
        when(result) {
            is SettingsResult.Logout.Success -> {
                host.exitToScene(SignInParams(), null, false, true)
            }
            is SettingsResult.Logout.Failure -> {
                scene.dismissLoginOutDialog()
                scene.showMessage(UIMessage(R.string.error_login_out))
            }
        }
    }

    private fun onGetUserSettings(result: SettingsResult.GetUserSettings){
        when(result) {
            is SettingsResult.GetUserSettings.Success -> {
                model.devices.clear()
                model.devices.addAll(result.userSettings.devices)
                model.isEmailConfirmed = result.userSettings.recoveryEmailConfirmationState
                model.recoveryEmail = result.userSettings.recoveryEmail
                deviceWrapperListController.update()
                scene.updateUserSettings(result.userSettings)
            }
            is SettingsResult.GetUserSettings.Failure -> {
                scene.showMessage(result.message)
            }
            is SettingsResult.GetUserSettings.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
            }
            is SettingsResult.GetUserSettings.Forbidden -> {
                scene.showConfirmPasswordDialog(settingsUIObserver)
            }
        }
    }

    private fun onRemoveDevice(result: SettingsResult.RemoveDevice){
        scene.removeDeviceDialogToggleLoad(false)
        when(result) {
            is SettingsResult.RemoveDevice.Success -> {
                deviceWrapperListController.remove(result.position)
                scene.removeDeviceDialogDismiss()
                scene.showMessage(UIMessage(R.string.device_removed))
            }
            is SettingsResult.RemoveDevice.Failure -> {
                scene.setRemoveDeviceError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onResetPassword(result: SettingsResult.ResetPassword){
        when(result) {
            is SettingsResult.ResetPassword.Success -> {
                scene.showMessage(UIMessage(R.string.forgot_password_message))
            }
            is SettingsResult.ResetPassword.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onDeviceDataUploaded(key: String, dataAddress: String) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthAccept(deviceId: Int, name: String) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {
            host.runOnUiThread(Runnable {
                scene.showLinkDeviceAuthConfirmation(untrustedDeviceInfo)
            })
        }

        override fun onNewEvent() {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                scene.showConfirmPasswordDialog(settingsUIObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }

}