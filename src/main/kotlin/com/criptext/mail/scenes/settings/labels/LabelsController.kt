package com.criptext.mail.scenes.settings.labels

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.Event
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
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
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.TransitionAnimationData
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
    : SceneController(host, activeAccount, storage){

    override val menuResourceId: Int? = null

    private val labelWrapperListController = LabelWrapperListController(model, scene.getLabelListView())

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.ActiveAccountUpdateMailbox -> onUpdateMailbox(result)
        }
    }

    private val dataSourceListener: (LabelsResult) -> Unit = { result ->
        when(result) {
            is LabelsResult.GetCustomLabels -> onGetCustomLabels(result)
            is LabelsResult.CreateCustomLabel -> onCreateCustomLabels(result)
            is LabelsResult.DeleteCustomLabel -> onDeleteCustomLabel(result)
            is LabelsResult.EditCustomLabel -> onEditCustomLabel(result)
        }
    }

    private val labelsUIObserver = object: LabelsUIObserver(generalDataSource, host){
        override fun onDeleteLabelClicked(label: LabelWrapper) {
            model.lastSelectedUUID = label.label.uuid
            scene.showLabelDeleteDialog(DialogData.DialogConfirmationData(
                    title = UIMessage(R.string.title_delete_label),
                    message = listOf(UIMessage(R.string.message_delete_label, arrayOf(label.text))),
                    type = DialogType.DeleteLabel()
            ))
        }

        override fun onEditLabelClicked(label: LabelWrapper) {
            model.lastSelectedUUID = label.label.uuid
            scene.showLabelEditDialog(DialogData.DialogDataForInput(
                    title = UIMessage(R.string.title_edit_label),
                    input = label.text,
                    type = DialogType.EditLabel()
            ))
        }

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
                        is DialogType.DeleteLabel -> {
                            dataSource.submitRequest(LabelsRequest.DeleteCustomLabel(model.lastSelectedUUID))
                        }
                    }
                }
                is DialogResult.DialogWithInput -> {
                    when(result.type){
                        is DialogType.EditLabel -> {
                            scene.labelEditDialogToggleLoad(true)
                            dataSource.submitRequest(LabelsRequest.EditCustomLabel(model.lastSelectedUUID, result.textInput))
                        }
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
                    keep = false
            )
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
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onChangeToNextAccount(result: GeneralResult.ChangeToNextAccount){
        when(result) {
            is GeneralResult.ChangeToNextAccount.Success -> {
                activeAccount = result.activeAccount
                generalDataSource.activeAccount = activeAccount
                generalDataSource.signalClient = SignalClient.Default(SignalStoreCriptext(generalDataSource.db, activeAccount))
                dataSource.activeAccount = activeAccount
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
                        keep = false, deletePastIntents = true
                )
            }
        }
    }

    private fun onUpdateMailbox(result: GeneralResult.ActiveAccountUpdateMailbox){
        when(result){
            is GeneralResult.ActiveAccountUpdateMailbox.Success -> {
                if (result.data != null) {
                    result.data.parsedEvents.forEach {
                        when(it.cmd){
                            Event.Cmd.peerLabelEdited,
                            Event.Cmd.peerLabelDeleted,
                            Event.Cmd.peerLabelCreated -> {
                                dataSource.submitRequest(LabelsRequest.GetCustomLabels())
                            }
                        }
                    }
                }
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

    private fun onDeleteCustomLabel(result: LabelsResult.DeleteCustomLabel){
        model.lastSelectedUUID = ""
        when(result) {
            is LabelsResult.DeleteCustomLabel.Success -> {
                val index = model.labels.indexOfFirst { it.label.uuid == result.uuid }
                if(index >= 0)
                    labelWrapperListController.remove(index)
            }
            is LabelsResult.DeleteCustomLabel.Failure -> {
                scene.showMessage(UIMessage(R.string.error_deleting_label))
            }
        }
    }

    private fun onEditCustomLabel(result: LabelsResult.EditCustomLabel){
        model.lastSelectedUUID = ""
        scene.labelEditDialogToggleLoad(false)
        scene.labelEditDialogDismiss()
        when(result) {
            is LabelsResult.EditCustomLabel.Success -> {
                val index = model.labels.indexOfFirst { it.label.uuid == result.uuid }
                if(index >= 0)
                    labelWrapperListController.updateName(result.newName, index)
            }
            is LabelsResult.EditCustomLabel.Failure -> {
                scene.showMessage(UIMessage(R.string.error_editing_label))
            }
        }
    }

    private fun onGetCustomLabels(result: LabelsResult.GetCustomLabels){
        when(result) {
            is LabelsResult.GetCustomLabels.Success -> {
                model.labels.clear()
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
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(labelsUIObserver, activeAccount.userEmail, dialogType)
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
            if(recipientId == activeAccount.recipientId && domain == activeAccount.domain) {
                host.runOnUiThread(Runnable {
                    generalDataSource.submitRequest(
                            GeneralRequest.ActiveAccountUpdateMailbox(Label.defaultItems.inbox,
                                    host.getLocalizedString(UIMessage(R.string.unable_to_decrypt)))
                    )
                })
            }
        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {
            host.runOnUiThread(Runnable {
                host.showConfirmPasswordDialog(labelsUIObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
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