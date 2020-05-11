package com.criptext.mail.scenes.settings.privacy

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton


class PrivacyController(
        private var activeAccount: ActiveAccount,
        private val model: PrivacyModel,
        private val scene: PrivacyScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.Logout -> onLogout(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.SetReadReceipts -> onReadReceipts(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.Set2FA -> onSet2FA(result)
            is GeneralResult.GetUserSettings -> onGetUserSettings(result)
            is GeneralResult.ChangeBlockRemoteContentSetting -> onBlockRemoteContentChanged(result)
        }
    }

    private val uiObserver = object: PrivacyUIObserver{
        override fun onBlockRemoteContentSwitched(isChecked: Boolean) {
            scene.enableBlockRemoteContentSwitch(false)
            generalDataSource.submitRequest(GeneralRequest.ChangeBlockRemoteContentSetting(isChecked))
        }

        override fun onTwoFASwitched(isChecked: Boolean) {
            if(model.isEmailConfirmed) {
                scene.enableTwoFASwitch(false)
                generalDataSource.submitRequest(GeneralRequest.Set2FA(isChecked))
            }else{
                scene.showMessage(UIMessage(R.string.message_warning_two_fa))
                scene.enableTwoFASwitch(true)
                scene.updateTwoFa(!isChecked)
            }
        }

        override fun onSnackbarClicked() {

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

        override fun onReadReceiptsSwitched(isChecked: Boolean) {
            scene.enableReadReceiptsSwitch(false)
            generalDataSource.submitRequest(GeneralRequest.SetReadReceipts(isChecked))
        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                        is DialogType.SignIn ->
                            host.goToScene(SignInParams(true), true)
                    }
                }
            }
        }

        override fun onOkButtonPressed(password: String) {
            generalDataSource.submitRequest(GeneralRequest.ConfirmPassword(password))
        }

        override fun onCancelButtonPressed() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(true))
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

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.goToScene(
                    params = SettingsParams(),
                    activityMessage = null,
                    forceAnimation = true,
                    keep = false
            )
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)

        scene.attachView(uiObserver, keyboardManager, model)
        scene.enableTwoFASwitch(false)
        scene.enableReadReceiptsSwitch(false)
        scene.enableBlockRemoteContentSwitch(false)
        generalDataSource.listener = generalDataSourceListener

        generalDataSource.submitRequest(GeneralRequest.GetUserSettings())


        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onLinkAccept(resultData: GeneralResult.LinkAccept){
        when (resultData) {
            is GeneralResult.LinkAccept.Success -> {
                host.goToScene(
                        params = LinkingParams(resultData.linkAccount, resultData.deviceId,
                        resultData.uuid, resultData.deviceType),
                        activityMessage = null,
                        keep = false, deletePastIntents = true
                )
            }
            is GeneralResult.LinkAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onSyncAccept(resultData: GeneralResult.SyncAccept){
        when (resultData) {
            is GeneralResult.SyncAccept.Success -> {
                host.goToScene(
                        params = LinkingParams(resultData.syncAccount, resultData.deviceId,
                        resultData.uuid, resultData.deviceType),
                        activityMessage = ActivityMessage.SyncMailbox(),
                        keep = false, deletePastIntents = true
                )
            }
            is GeneralResult.SyncAccept.Failure -> {
                scene.showMessage(resultData.message)
            }
        }
    }

    private fun onReadReceipts(result: GeneralResult.SetReadReceipts){
        when(result) {
            is GeneralResult.SetReadReceipts.Success -> {
                scene.enableReadReceiptsSwitch(true)
            }
            is GeneralResult.SetReadReceipts.Failure -> {
                scene.showMessage(result.message)
                scene.enableReadReceiptsSwitch(true)
                scene.updateReadReceipts(!result.readReceiptAttempt)
            }
        }
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                websocketEvents = if(jwts.isNotEmpty())
                    WebSocketSingleton.getInstance(jwts)
                else
                    WebSocketSingleton.getInstance(activeAccount.jwt)

                websocketEvents.setListener(webSocketEventListener)

                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))
                host.goToScene(
                        params = MailboxParams(),
                        activityMessage = null,
                        keep = false,
                        deletePastIntents = true
                )
            }
        }
    }

    private fun onSet2FA(result: GeneralResult.Set2FA){
        when(result){
            is GeneralResult.Set2FA.Success -> {
                if(result.hasTwoFA)
                    scene.showTwoFADialog(model.isEmailConfirmed)
                scene.enableTwoFASwitch(true)
            }
            is GeneralResult.Set2FA.Failure -> {
                scene.updateTwoFa(!result.twoFAAttempt)
                scene.enableTwoFASwitch(true)
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
                if(result.activeAccount == null)
                    host.goToScene(
                            params = SignInParams(), keep = false,
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.device_removed_remotely_exception)),
                            forceAnimation = true, deletePastIntents = true
                    )
                else {
                    activeAccount = result.activeAccount
                    host.goToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            keep = false, deletePastIntents = true
                    )
                }
            }
        }
    }

    private fun onLogout(result: GeneralResult.Logout){
        when (result) {
            is GeneralResult.Logout.Success -> {
                CloudBackupJobService.cancelJob(storage, result.oldAccountId)
                if(result.activeAccount == null)
                    host.goToScene(
                            params = SignInParams(), keep = false,
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.expired_session)),
                            forceAnimation = true, deletePastIntents = true
                    )
                else {
                    activeAccount = result.activeAccount
                    host.goToScene(
                            params = MailboxParams(),
                            activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail))),
                            keep = false, deletePastIntents = true
                    )
                }

            }
        }
    }

    private fun onGetUserSettings(result: GeneralResult.GetUserSettings){
        when(result) {
            is GeneralResult.GetUserSettings.Success -> {
                model.isEmailConfirmed = result.userSettings.recoveryEmailConfirmationState
                model.twoFA = result.userSettings.hasTwoFA
                model.readReceipts = result.userSettings.hasReadReceipts
                model.blockRemoteContent = result.userSettings.blockRemoteContent
                scene.enableReadReceiptsSwitch(true)
                scene.updateReadReceipts(model.readReceipts)
                scene.enableTwoFASwitch(true)
                scene.updateTwoFa(model.twoFA)
                scene.enableBlockRemoteContentSwitch(true)
                scene.updateBlockRemoteContent(model.blockRemoteContent)
            }
            is GeneralResult.GetUserSettings.Failure -> {
                scene.showMessage(result.message)
            }
            is GeneralResult.GetUserSettings.Unauthorized -> {
                generalDataSource.submitRequest(GeneralRequest.Logout(false, false))
            }
            is GeneralResult.GetUserSettings.Forbidden -> {
                scene.showConfirmPasswordDialog(uiObserver)
            }
            is GeneralResult.GetUserSettings.EnterpriseSuspended -> {
                showSuspendedAccountDialog()
            }
        }
    }

    private fun onBlockRemoteContentChanged(result: GeneralResult.ChangeBlockRemoteContentSetting){
        when(result) {
            is GeneralResult.ChangeBlockRemoteContentSetting.Success -> {
                scene.enableBlockRemoteContentSwitch(true)
                if(!result.newBlockRemoteContent) {
                    scene.showMessage(UIMessage(R.string.block_remote_turn_off_message,
                            arrayOf(activeAccount.userEmail)))
                } else {
                    scene.showMessage(UIMessage(R.string.block_remote_turn_off_message_disable,
                            arrayOf(activeAccount.userEmail)))
                }
            }
            is GeneralResult.ChangeBlockRemoteContentSetting.Failure -> {
                scene.updateBlockRemoteContent(!result.newBlockRemoteContent)
                scene.enableBlockRemoteContentSwitch(true)
                scene.showMessage(result.message)
            }
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

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(uiObserver, activeAccount.userEmail, dialogType)
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