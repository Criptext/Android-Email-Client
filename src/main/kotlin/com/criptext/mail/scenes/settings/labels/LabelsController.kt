package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.labels.data.LabelWrapperListController
import com.criptext.mail.scenes.settings.labels.data.LabelsDataSource
import com.criptext.mail.scenes.settings.labels.data.LabelsRequest
import com.criptext.mail.scenes.settings.labels.data.LabelsResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton

class LabelsController(
        private val model: LabelsModel,
        private val scene: LabelsScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: LabelsDataSource)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val labelWrapperListController = LabelWrapperListController(model, scene.getLabelListView())

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val dataSourceListener: (LabelsResult) -> Unit = { result ->
        when(result) {
            is LabelsResult.GetCustomLabels -> onGetCustomLabels(result)
            is LabelsResult.CreateCustomLabel -> onCreateCustomLabels(result)
        }
    }

    private val labelsUIObserver = object: LabelsUIObserver{
        override fun onToggleLabelSelection(label: LabelWrapper) {
            dataSource.submitRequest(LabelsRequest.ChangeVisibilityLabel(label.id, label.isSelected))
        }

        override fun onCreateLabelClicked() {
            scene.showCreateLabelDialog(keyboardManager)
        }

        override fun onCustomLabelNameAdded(labelName: String) {
            dataSource.submitRequest(LabelsRequest.CreateCustomLabel(labelName))
            keyboardManager.hideKeyboard()
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

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogConfirmation -> {
                    when(result.type){
                        is DialogType.SwitchAccount -> {
                            generalDataSource.submitRequest(GeneralRequest.ChangeToNextAccount())
                        }
                    }
                }
            }
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

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,false)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(labelsUIObserver, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener

        if(model.labels.isEmpty()) {
            dataSource.submitRequest(LabelsRequest.GetCustomLabels())
        }

        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
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

    private fun onCreateCustomLabels(result: LabelsResult.CreateCustomLabel){
        when(result) {
            is LabelsResult.CreateCustomLabel.Success -> {
                val labelWrapper = LabelWrapper(result.label)
                labelWrapper.isSelected = true
                labelWrapperListController.addNew(labelWrapper)
            }
            is LabelsResult.CreateCustomLabel.Failure -> {
                scene.showMessage(UIMessage(R.string.error_creating_labels))
                host.finishScene()
            }
        }
    }

    private fun onGetCustomLabels(result: LabelsResult.GetCustomLabels){
        when(result) {
            is LabelsResult.GetCustomLabels.Success -> {
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
            is LabelsResult.GetCustomLabels.Failure -> {
                scene.showMessage(UIMessage(R.string.error_getting_labels))
                host.finishScene()
            }
        }
    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val showButton = jwtList.isNotEmpty() && jwtList.size > 1
        scene.showAccountSuspendedDialog(labelsUIObserver, activeAccount.userEmail, showButton)
    }

    private val webSocketEventListener = object : WebSocketEventListener {
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
                scene.showConfirmPasswordDialog(labelsUIObserver)
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
        labelsUIObserver.onBackButtonPressed()
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