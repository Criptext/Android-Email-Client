package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration

import android.content.ClipData
import android.content.ClipboardManager
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.CustomDomainParams
import com.criptext.mail.scenes.params.LinkingParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data.DomainConfigurationDataSource
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data.DomainConfigurationRequest
import com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration.data.DomainConfigurationResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
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

class DomainConfigurationController(
        private val model: DomainConfigurationModel,
        private val scene: DomainConfigurationScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val clipboardManager: ClipboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: DomainConfigurationDataSource)
    : SceneController(host, activeAccount, storage){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val uiObserver = object: DomainConfigurationUIObserver(generalDataSource, host){
        override fun onCopyButtonClicked(text: String) {
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.primaryClip = clipData
            scene.showMessage(UIMessage(R.string.mx_records_copy_clipboard))
        }

        override fun onNextButtonPressed() {
            when(model.state.first) {
                DomainConfigurationModel.StepState.FIRST -> {
                    scene.progressNextButton(true)
                    dataSource.submitRequest(DomainConfigurationRequest.GetMXRecords(model.domain.name))
                }
                DomainConfigurationModel.StepState.SECOND -> {
                    scene.progressNextButton(true)
                    scene.hideBackButton(true)
                    model.state = Pair(DomainConfigurationModel.StepState.THIRD, R.layout.activity_domain_configuration_step_3)
                    scene.initState(model)
                    scene.progressNextButton(false)
                    scene.enableNextButton(false)
                    scene.setProgress(10)
                    dataSource.submitRequest(DomainConfigurationRequest.ValidateDomain(model.domain.name))
                }
                DomainConfigurationModel.StepState.THIRD -> {
                    if(model.validationSuccess){
                        scene.progressNextButton(true)
                        host.goToScene(
                                params = CustomDomainParams(),
                                activityMessage = ActivityMessage.DomainRegistered(model.domain),
                                keep = false
                        )
                    } else if(model.validationError) {
                        onBackButtonPressed()
                    }
                }
            }
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
            when(model.state.first){
                DomainConfigurationModel.StepState.FIRST -> {
                    keyboardManager.hideKeyboard()
                    host.finishScene()
                }
                DomainConfigurationModel.StepState.SECOND -> {
                    model.state = Pair(DomainConfigurationModel.StepState.FIRST, R.layout.activity_domain_configuration_step_1)
                    scene.initState(model)
                    scene.enableNextButton(true)
                }
                DomainConfigurationModel.StepState.THIRD -> {
                    if(model.validationError || model.validationSuccess) {
                        model.state = Pair(DomainConfigurationModel.StepState.SECOND, R.layout.activity_domain_configuration_step_2)
                        scene.initState(model)
                        scene.enableNextButton(false)
                        model.validationError = false
                        model.validationSuccess = false
                        model.retryTimeValidateRecords = 0
                    }
                }
            }
        }
    }

    private val dataSourceListener = { result: DomainConfigurationResult ->
        when (result) {
            is DomainConfigurationResult.GetMXRecords -> onGetMXRecords(result)
            is DomainConfigurationResult.ValidateDomain -> onValidateDomain(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(uiObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        scene.enableNextButton(true)
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onValidateDomain(result: DomainConfigurationResult.ValidateDomain){
        when(result){
            is DomainConfigurationResult.ValidateDomain.Success -> {
                scene.setProgress(100)
                scene.enableNextButton(true)
                model.validationSuccess = true
                model.domain.validated = true
            }
            is DomainConfigurationResult.ValidateDomain.Failure -> {
                if(model.retryTimeValidateRecords < RETRY_TIMES_DEFAULT) {
                    model.retryTimeValidateRecords++
                    scene.setProgress(10 + (model.retryTimeValidateRecords * 10))
                    host.postDelay(Runnable{
                        dataSource.submitRequest(DomainConfigurationRequest.ValidateDomain(model.domain.name))
                    }, RETRY_TIME_DEFAULT)
                } else {
                    model.validationError = true
                    scene.setProgressError(result.errorCode, model.domain.name)
                    scene.enableNextButton(true)
                    scene.hideBackButton(false)
                }
            }
        }
    }

    private fun onGetMXRecords(result: DomainConfigurationResult.GetMXRecords){
        scene.progressNextButton(false)
        when(result){
            is DomainConfigurationResult.GetMXRecords.Success -> {
                model.state = Pair(DomainConfigurationModel.StepState.SECOND, R.layout.activity_domain_configuration_step_2)
                model.mxRecords = result.mxRecords
                scene.initState(model)
                scene.enableNextButton(false)
            }
            is DomainConfigurationResult.GetMXRecords.NotFound -> {
                host.finishScene()
            }
            is DomainConfigurationResult.GetMXRecords.Failure -> {
                scene.enableNextButton(false)
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

                host.goToScene(
                        params = MailboxParams(),
                        activityMessage = null,
                        keep = false,
                        deletePastIntents = true
                )
            }
        }
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
        uiObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val RETRY_TIME_DEFAULT = 30000L
        const val RETRY_TIMES_DEFAULT = 6
    }
}