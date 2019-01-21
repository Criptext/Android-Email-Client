package com.criptext.mail.scenes.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.BaseActivity
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.scenes.settings.devices.DeviceWrapperListController
import com.criptext.mail.scenes.settings.labels.LabelWrapperListController
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketController
import com.criptext.mail.websocket.WebSocketEventListener
import java.util.*

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
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.SyncPhonebook -> onSyncPhonebook(result)
            is GeneralResult.Logout -> onLogout(result)
            is GeneralResult.DeleteAccount -> onDeleteAccount(result)
            is GeneralResult.SyncStatus -> onSyncStatus(result)
        }
    }

    private val dataSourceListener = { result: SettingsResult ->
        when (result) {
            is SettingsResult.ChangeContactName -> onContactNameChanged(result)
            is SettingsResult.GetCustomLabels -> onGetCustomLabels(result)
            is SettingsResult.CreateCustomLabel -> onCreateCustomLabels(result)
            is SettingsResult.GetUserSettings -> onGetUserSettings(result)
            is SettingsResult.RemoveDevice -> onRemoveDevice(result)
            is SettingsResult.ResetPassword -> onResetPassword(result)
            is SettingsResult.Set2FA -> onSet2FA(result)
            is SettingsResult.SyncBegin -> onSyncBegin(result)
        }
    }

    private val settingsUIObserver = object: SettingsUIObserver{

        override fun onSnackbarClicked() {

        }

        override fun onSyncMailboxCanceled() {
            model.isWaitingForSync = false
        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if(trustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.SyncAccept(trustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            generalDataSource.submitRequest(GeneralRequest.SyncDenied(trustedDeviceInfo))
        }

        override fun onDarkThemeSwitched(isChecked: Boolean) {
            storage.putBool(KeyValueStorage.StringKey.HasDarkTheme, isChecked)
            model.devices.clear()
            model.hasChangedTheme = true
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                host.setAppTheme(R.style.DarkAppTheme)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                host.setAppTheme(R.style.AppTheme)
            }
        }

        override fun onResendDeviceLinkAuth() {
            dataSource.submitRequest(SettingsRequest.SyncBegin())
        }

        override fun onSyncMailbox() {
            scene.showGeneralDialogConfirmation(DialogData.DialogConfirmationData(
                        title = UIMessage(R.string.sync_confirmation_dialog_title),
                        message = listOf(UIMessage(R.string.sync_confirmation_dialog_message)),
                        type = DialogType.ManualSyncConfirmation()
                ))
        }

        override fun onDeleteAccountClicked() {
            scene.showGeneralDialogWithInputPassword(DialogData.DialogMessageData(
                    title = UIMessage(R.string.delete_account_dialog_title),
                    message = listOf(UIMessage(R.string.delete_account_dialog_message)),
                    type = DialogType.DeleteAccount()
            ))
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogWithInput -> {
                    when(result.type){
                        is DialogType.DeleteAccount -> {
                            scene.toggleGeneralDialogLoad(true)
                            generalDataSource.submitRequest(GeneralRequest.DeleteAccount(result.textInput))
                        }
                    }
                }
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.ManualSyncConfirmation -> {
                            scene.showSyncBeginDialog()
                            dataSource.submitRequest(SettingsRequest.SyncBegin())
                            model.isWaitingForSync = true
                        }
                    }
                }
            }
        }

        override fun onSyncPhonebookContacts() {
            if (host.checkPermissions(BaseActivity.RequestCode.readAccess.ordinal,
                            Manifest.permission.READ_CONTACTS)) {
                storage.putBool(KeyValueStorage.StringKey.UserHasAcceptedPhonebookSync, true)
                scene.setSyncContactsProgressVisisble(true)
                val resolver = host.getContentResolver()
                if (resolver != null)
                    generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
            }
        }

        override fun onPinLockClicked() {
            host.goToScene(PrivacyAndSecurityParams(model.hasReadReceipts), false)
        }

        override fun onTwoFASwitched(isChecked: Boolean) {
            if(model.isEmailConfirmed) {
                scene.enableTwoFASwitch(false)
                dataSource.submitRequest(SettingsRequest.Set2FA(isChecked))
            }else{
                scene.enableTwoFASwitch(true)
                scene.updateTwoFa(!isChecked)
            }
            if(isChecked)
                scene.showTwoFADialog(model.isEmailConfirmed)
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            if(untrustedDeviceInfo.syncFileVersion == UserDataWriter.FILE_SYNC_VERSION)
                generalDataSource.submitRequest(GeneralRequest.LinkAccept(untrustedDeviceInfo))
            else
                scene.showMessage(UIMessage(R.string.sync_version_incorrect))
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
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

        override fun onReplyToChangeClicked() {
            host.goToScene(ReplyToParams(model.replyToEmail ?: ""), false)
        }

        override fun onPrivacyPoliciesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/${Locale.getDefault().language}/privacy")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onTermsOfServiceClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/${Locale.getDefault().language}/terms")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onOpenSourceLibrariesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/${Locale.getDefault().language}/open-source-android")
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }
        override fun onLogoutClicked() {
            val isLastDeviceWith2FA = model.devices.size == 1 &&
                    model.hasTwoFA
            scene.showLogoutDialog(isLastDeviceWith2FA)
        }

        override fun onLogoutConfirmedClicked() {
            scene.showMessageAndProgressDialog(UIMessage(R.string.login_out_dialog_message))
            generalDataSource.submitRequest(GeneralRequest.Logout(false))
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
            if(model.hasChangedTheme) {
                host.exitToScene(MailboxParams(), null, false, true)
            }
            else{
                host.finishScene()
            }
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

    private val onDevicesListItemListener:DevicesListItemListener = object: DevicesListItemListener {
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
                    deviceType = DeviceUtils.getDeviceType().ordinal,
                    lastActivity = null))
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
        settingsUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != BaseActivity.RequestCode.readAccess.ordinal) return

        val indexOfPermission = permissions.indexOfFirst { it == Manifest.permission.READ_CONTACTS }
        if (indexOfPermission < 0) return
        if (grantResults[indexOfPermission] != PackageManager.PERMISSION_GRANTED) {
            scene.showMessage(UIMessage(R.string.sync_phonebook_permission))
            return
        }
        val resolver = host.getContentResolver()
        if(resolver != null)
            generalDataSource.submitRequest(GeneralRequest.SyncPhonebook(resolver))
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
                scene.setConfirmPasswordError(result.message)
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

    private fun onSyncPhonebook(resultData: GeneralResult.SyncPhonebook){
        scene.setSyncContactsProgressVisisble(false)
        when (resultData) {
            is GeneralResult.SyncPhonebook.Success -> {
                scene.showMessage(UIMessage(R.string.sync_phonebook_text))
            }
            is GeneralResult.SyncPhonebook.Failure -> {
                scene.showMessage(resultData.message)
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
                    if(it.type == LabelTypes.SYSTEM) {
                        val label = it.copy(
                                text = scene.getLabelLocalizedName(it.text)
                        )
                        val labelWrapper = LabelWrapper(label)
                        labelWrapper.isSelected = it.visible
                        labelWrapper
                    }else {
                        val labelWrapper = LabelWrapper(it)
                        labelWrapper.isSelected = it.visible
                        labelWrapper
                    }
                })
                labelWrapperListController.notifyDataSetChange()
            }
            is SettingsResult.GetCustomLabels.Failure -> {
                scene.showMessage(UIMessage(R.string.error_getting_labels))
                host.finishScene()
            }
        }
    }

    private fun onLogout(result: GeneralResult.Logout){
        when(result) {
            is GeneralResult.Logout.Success -> {
                host.exitToScene(SignInParams(), null, false, true)
            }
            is GeneralResult.Logout.Failure -> {
                scene.dismissMessageAndProgressDialog()
                scene.showMessage(UIMessage(R.string.error_login_out))
            }
        }
    }

    private fun onDeleteAccount(result: GeneralResult.DeleteAccount){
        scene.toggleGeneralDialogLoad(false)
        when(result) {
            is GeneralResult.DeleteAccount.Success -> {
                host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.delete_account_toast_message)), false, true)
            }
            is GeneralResult.DeleteAccount.Failure -> {
                scene.setGeneralDialogWithInputError(result.message)
            }
        }
    }

    private fun onSyncBegin(result: SettingsResult.SyncBegin){
        scene.enableSyncBeginResendButton()
        when(result) {
            is SettingsResult.SyncBegin.Success -> {
                generalDataSource.submitRequest(GeneralRequest.SyncStatus())
            }
            is SettingsResult.SyncBegin.Failure -> {
                scene.setGeneralDialogWithInputError(result.message)
            }
        }
    }

    private fun onGetUserSettings(result: SettingsResult.GetUserSettings){
        when(result) {
            is SettingsResult.GetUserSettings.Success -> {
                model.devices.clear()
                model.devices.addAll(result.userSettings.devices)
                model.isEmailConfirmed = result.userSettings.recoveryEmailConfirmationState
                model.replyToEmail = result.userSettings.replyTo
                model.recoveryEmail = result.userSettings.recoveryEmail
                model.hasTwoFA = result.userSettings.hasTwoFA
                model.hasReadReceipts = result.userSettings.hasReadReceipts
                deviceWrapperListController.update()
                scene.updateUserSettings(model)
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

    private fun onSet2FA(result: SettingsResult.Set2FA){
        when(result) {
            is SettingsResult.Set2FA.Success -> {
                scene.enableTwoFASwitch(true)
                scene.updateTwoFa(result.hasTwoFA)
                model.hasTwoFA = result.hasTwoFA
            }
            is SettingsResult.Set2FA.Failure -> {
                scene.showMessage(result.message)
                scene.enableTwoFASwitch(true)
                scene.updateTwoFa(!result.twoFAAttempt)
                model.hasTwoFA = !result.twoFAAttempt
            }
        }
    }

    private fun onSyncStatus(result: GeneralResult.SyncStatus) {
        if(model.isWaitingForSync) {
            when (result) {
                is GeneralResult.SyncStatus.Success -> {
                    model.isWaitingForSync = false
                    model.retryTimeLinkStatus = 0
                    host.goToScene(SyncingParams(activeAccount.userEmail, result.syncStatusData.authorizerId,
                            result.syncStatusData.randomId, result.syncStatusData.authorizerType,
                            result.syncStatusData.authorizerName), true)
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
                    scene.syncBeginDialogDenied()
                    model.retryTimeLinkStatus = 0
                }
            }
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            if (!model.isWaitingForSync){
                host.runOnUiThread(Runnable {
                    scene.showSyncDeviceAuthConfirmation(trustedDeviceInfo)
                })
            }
        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {
            if(model.isWaitingForSync){
                model.isWaitingForSync = false
                host.getHandler()?.removeCallbacks(null)
                host.goToScene(SyncingParams(activeAccount.userEmail, syncStatusData.authorizerId,
                        syncStatusData.randomId, syncStatusData.authorizerType,
                        syncStatusData.authorizerName), true)
            }
        }

        override fun onSyncRequestDeny() {
            if(model.isWaitingForSync){
                model.isWaitingForSync = false
                host.runOnUiThread(Runnable {
                    scene.syncBeginDialogDenied()
                })
            }
        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
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
    companion object {
        const val RETRY_TIME_DEFAULT = 5000L
        const val RETRY_TIMES_DEFAULT = 12
    }

}