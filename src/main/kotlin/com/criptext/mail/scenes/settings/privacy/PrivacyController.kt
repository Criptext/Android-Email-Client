package com.criptext.mail.scenes.settings.privacy

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.TransitionAnimationData
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
    : SceneController(host, activeAccount, storage){

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

    private val uiObserver = object: PrivacyUIObserver(generalDataSource, host){
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
                scene.showMessage(result.message)
            }
            is GeneralResult.GetUserSettings.SessionExpired -> {
                generalDataSource.submitRequest(GeneralRequest.Logout(true, false))
            }
            is GeneralResult.GetUserSettings.Forbidden -> {
                host.showConfirmPasswordDialog(uiObserver)
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
                host.showConfirmPasswordDialog(uiObserver)
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