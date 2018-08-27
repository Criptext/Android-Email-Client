package com.criptext.mail.scenes.settings

import android.content.Intent
import com.criptext.mail.BaseActivity
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
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
import com.criptext.mail.utils.remotechange.data.RemoteChangeRequest
import com.criptext.mail.utils.remotechange.data.RemoteChangeResult
import com.criptext.mail.validation.FormInputState

class SettingsController(
        private val model: SettingsModel,
        private val scene: SettingsScene,
        private val host: IHostActivity,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val remoteChangeDataSource: BackgroundWorkManager<RemoteChangeRequest, RemoteChangeResult>,
        private val dataSource: BackgroundWorkManager<SettingsRequest, SettingsResult>)
    : SceneController(){

    override val menuResourceId: Int? = null
    val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    private val labelWrapperListController = LabelWrapperListController(model, scene.getLabelListView())
    private val deviceWrapperListController = DeviceWrapperListController(model, scene.getDeviceListView())


    private val remoteChangeDataSourceListener: (RemoteChangeResult) -> Unit = { result ->
        when(result) {
            is RemoteChangeResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is RemoteChangeResult.ConfirmPassword -> onPasswordChangedRemotely(result)
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
            is SettingsResult.ChangePassword -> onChangePassword(result)
        }
    }

    private val settingsUIObserver = object: SettingsUIObserver{
        override fun onOkButtonPressed(password: String) {
            remoteChangeDataSource.submitRequest(RemoteChangeRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            remoteChangeDataSource.submitRequest(RemoteChangeRequest.DeviceRemoved())
        }

        override fun onRecoveryEmailOptionClicked() {
            host.goToScene(RecoveryEmailParams(model.isEmailCconfirmed, model.recoveryEmail), false)
        }

        override fun onOldPasswordChangedListener(password: String) {
            model.oldPasswordText = password
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

        override fun onOkChangePasswordDialogButton() {
            dataSource.submitRequest(SettingsRequest.ChangePassword(model.oldPasswordText, model.confirmPasswordText))
        }

        override fun onChangePasswordOptionClicked() {
            scene.showChangePasswordDialog()
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

        override fun onRemoveDeviceConfirmed(deviceId: Int, position: Int) {
            dataSource.submitRequest(SettingsRequest.RemoveDevice(deviceId, position))
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
        override fun onDeviceLongClicked(device: DeviceItem, position: Int): Boolean {
            settingsUIObserver.onRemoveDevice(device.id, position)
            return true
        }
    }

    private fun checkPasswords(passwords: Pair<String, String>) {
        if (arePasswordsMatching && passwords.first.length >= minimumPasswordLength) {
            scene.showPasswordDialogError(null)
            scene.toggleChangePasswordSuccess(show = true)
            model.passwordState = FormInputState.Valid()
            if (model.passwordState is FormInputState.Valid)
                scene.toggleChangePasswordButton(true)
        } else if (arePasswordsMatching && passwords.first.isEmpty()) {
            scene.showPasswordDialogError(null)
            scene.toggleChangePasswordSuccess(show = false)
            model.passwordState = FormInputState.Unknown()
            scene.toggleChangePasswordButton(false)
        } else if (arePasswordsMatching && passwords.first.length < minimumPasswordLength) {
            scene.toggleChangePasswordSuccess(show = false)
            val errorMessage = UIMessage(R.string.password_length_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.showPasswordDialogError(errorMessage)
            scene.toggleChangePasswordButton(false)
        } else {
            val errorMessage = UIMessage(R.string.password_mismatch_error)
            model.passwordState = FormInputState.Error(errorMessage)
            scene.showPasswordDialogError(errorMessage)
            scene.toggleChangePasswordSuccess(show = false)
            scene.toggleChangePasswordButton(false)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        remoteChangeDataSource.listener = remoteChangeDataSourceListener
        model.fullName = activeAccount.name
        model.signature = activeAccount.signature
        if(model.labels.isEmpty()) {
            dataSource.submitRequest(SettingsRequest.GetCustomLabels())
        }
        if(model.devices.isEmpty()) {
            model.devices.add(DeviceItem(
                    id = activeAccount.deviceId,
                    friendlyName = DeviceUtils.getDeviceName(),
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

    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    private fun onDeviceRemovedRemotely(result: RemoteChangeResult.DeviceRemoved){
        when (result) {
            is RemoteChangeResult.DeviceRemoved.Success -> {
                host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)), true, true)
            }
        }
    }

    private fun onPasswordChangedRemotely(result: RemoteChangeResult.ConfirmPassword){
        when (result) {
            is RemoteChangeResult.ConfirmPassword.Success -> {
                scene.dismissConfirmPasswordDialog()
                scene.showMessage(UIMessage(R.string.update_password_success))
            }
            is RemoteChangeResult.ConfirmPassword.Failure -> {
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
                model.isEmailCconfirmed = result.userSettings.recoveryEmailConfirmationState
                model.recoveryEmail = result.userSettings.recoveryEmail
                deviceWrapperListController.update()
                scene.updateUserSettings(result.userSettings)
            }
            is SettingsResult.GetUserSettings.Failure -> {
                scene.showMessage(result.message)
            }
            is SettingsResult.GetUserSettings.Unauthorized -> {
                remoteChangeDataSource.submitRequest(RemoteChangeRequest.DeviceRemoved())
            }
            is SettingsResult.GetUserSettings.Forbidden -> {
                scene.showConfirmPasswordDialog(settingsUIObserver)
            }
        }
    }

    private fun onRemoveDevice(result: SettingsResult.RemoveDevice){
        scene.dismissRemovingDeviceDialog()
        when(result) {
            is SettingsResult.RemoveDevice.Success -> {
                deviceWrapperListController.remove(result.position)
                scene.showMessage(UIMessage(R.string.device_removed))
            }
            is SettingsResult.RemoveDevice.Failure -> {
                scene.showMessage(UIMessage(R.string.error_listing_devices))
            }
        }
    }

    private fun onChangePassword(result: SettingsResult.ChangePassword){
        when(result) {
            is SettingsResult.ChangePassword.Success -> {
                scene.dismissEnterPasswordDialog()
                scene.showMessage(UIMessage(R.string.change_password_success))
            }
            is SettingsResult.ChangePassword.Failure -> {
                scene.setConfirmPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    companion object {
        val minimumPasswordLength = 8
    }

}