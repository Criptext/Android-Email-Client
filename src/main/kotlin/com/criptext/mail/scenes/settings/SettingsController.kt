package com.criptext.mail.scenes.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
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
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.composer.data.ComposerType
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.data.SettingsDataSource
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.mailtemplates.CriptextMailTemplate
import com.criptext.mail.utils.mailtemplates.ReportAbuseMailTemplate
import com.criptext.mail.utils.mailtemplates.SupportMailTemplate
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketController
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketSingleton
import java.util.*

class SettingsController(
        private val model: SettingsModel,
        private val scene: SettingsScene,
        private val host: IHostActivity,
        private var websocketEvents: WebSocketController,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: SettingsDataSource)
    : SceneController(){

    override val menuResourceId: Int? = null


    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.SyncPhonebook -> onSyncPhonebook(result)
            is GeneralResult.SyncStatus -> onSyncStatus(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val dataSourceListener = { result: SettingsResult ->
        when (result) {
            is SettingsResult.ResetPassword -> onResetPassword(result)
            is SettingsResult.SyncBegin -> onSyncBegin(result)
        }
    }

    private val settingsUIObserver = object: SettingsUIObserver{
        override fun onAliasesClicked() {
            host.goToScene(AliasesParams(), true)
        }

        override fun onCustomDomainClicked() {
            host.goToScene(CustomDomainParams(), true)
        }

        override fun onReportBugClicked() {
            host.goToScene(ComposerParams(type = ComposerType.Support(
                    host.getMailTemplate(CriptextMailTemplate.TemplateType.SUPPORT) as SupportMailTemplate), currentLabel = Label.defaultItems.inbox),
                    true)
        }

        override fun onReportAbuseClicked() {
            host.goToScene(ComposerParams(type = ComposerType.Report(
                    host.getMailTemplate(CriptextMailTemplate.TemplateType.ABUSE) as ReportAbuseMailTemplate), currentLabel = Label.defaultItems.inbox),
                    true)
        }

        override fun onCloudBackupClicked() {
            host.goToScene(CloudBackupParams(), false)
        }

        override fun onLabelsOptionClicked() {
            host.goToScene(LabelsParams(), false)
        }

        override fun onDevicesOptionClicked() {
            host.goToScene(DevicesParams(), false)
        }

        override fun onPrivacyClicked() {
            host.goToScene(PrivacyParams(), false)
        }

        override fun onShowPreviewSwitched(isChecked: Boolean) {
            storage.putBool(KeyValueStorage.StringKey.ShowEmailPreview, isChecked)
        }

        override fun onSnackbarClicked() {

        }

        override fun onSyncMailboxCanceled() {
            model.isWaitingForSync = false
            generalDataSource.submitRequest(GeneralRequest.SyncCancel())
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
            model.hasChangedTheme = true
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                host.setAppTheme(R.style.DarkAppTheme)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                host.setAppTheme(R.style.AppTheme)
            }
            host.exitToScene(SettingsParams(true), null, false, true)
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

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.ManualSyncConfirmation -> {
                            scene.showSyncBeginDialog()
                            dataSource.submitRequest(SettingsRequest.SyncBegin())
                            model.isWaitingForSync = true
                        }
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn -> {
                            host.goToScene(SignInParams(true), true)
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
            host.goToScene(PinLockParams(), false)
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

        override fun onAccountOptionClicked() {
            host.goToScene(ProfileParams(false), false)
        }

        override fun onFAQClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("faq"))
        }

        override fun onPrivacyPoliciesClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("privacy"))
        }

        override fun onTermsOfServiceClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("terms"))
        }

        override fun onOpenSourceLibrariesClicked() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("open-source-android"))
        }

        override fun onBackButtonPressed() {
            if(model.hasChangedTheme) {
                host.exitToScene(MailboxParams(), null, false, true)
            }
            else{
                host.finishScene()
            }
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        checkFirstTimeToResetCache()
        websocketEvents.setListener(webSocketEventListener)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener

        scene.attachView(
                email = activeAccount.userEmail,
                model = model,
                settingsUIObserver = settingsUIObserver)

        model.showEmailPreview = storage.getBool(KeyValueStorage.StringKey.ShowEmailPreview, true)
        scene.setEmailPreview(model.showEmailPreview)

        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun checkFirstTimeToResetCache(){
        if(!storage.getBool(KeyValueStorage.StringKey.HasTimestampForCacheReset, false)){
            storage.putBool(KeyValueStorage.StringKey.HasTimestampForCacheReset, true)
            storage.putLong(KeyValueStorage.StringKey.CacheResetTimestamp, System.currentTimeMillis())
        }
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
                if(result.activeAccount == null)
                    host.exitToScene(SignInParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                            true, true)
                else {
                    activeAccount = result.activeAccount
                    host.exitToScene(MailboxParams(),
                            ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            false, true)
                }
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
                host.exitToScene(LinkingParams(resultData.linkAccount, resultData.deviceId,
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
                host.exitToScene(LinkingParams(resultData.syncAccount, resultData.deviceId,
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

    private fun onSyncBegin(result: SettingsResult.SyncBegin){
        scene.enableSyncBeginResendButton()
        when(result) {
            is SettingsResult.SyncBegin.Success -> {
                generalDataSource.submitRequest(GeneralRequest.SyncStatus())
            }
            is SettingsResult.SyncBegin.Failure -> {
                scene.showMessage(result.message)
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

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                dataSource.activeAccount = activeAccount
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                websocketEvents = if(jwts.isNotEmpty())
                    WebSocketSingleton.getInstance(jwts)
                else
                    WebSocketSingleton.getInstance(activeAccount.jwt)

                websocketEvents.setListener(webSocketEventListener)

                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))

                host.exitToScene(MailboxParams(), null, false, true)
            }
        }
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(settingsUIObserver, activeAccount.userEmail, dialogType)
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
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    showSuspendedAccountDialog()
            })
        }

        override fun onAccountUnsuspended(accountEmail: String) {
            host.runOnUiThread(Runnable {
                if (accountEmail == activeAccount.userEmail)
                    scene.dismissAccountSuspendedDialog()
            })
        }

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

        override fun onNewEvent(recipientId: String, domain: String) {

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