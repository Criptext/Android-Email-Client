package com.criptext.mail.scenes.settings.custom_domain_entry

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryDataSource
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryRequest
import com.criptext.mail.scenes.settings.custom_domain_entry.data.CustomDomainEntryResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogData
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton

class CustomDomainEntryController(
        private val model: CustomDomainEntryModel,
        private val scene: CustomDomainEntryScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: CustomDomainEntryDataSource)
    : SceneController(){

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

    private val uiObserver = object: CustomDomainEntryUIObserver{
        override fun onNextButtonPressed() {
            scene.progressNextButton(true)
            dataSource.submitRequest(CustomDomainEntryRequest.CheckDomainAvailability(model.newDomain.value))
        }

        override fun onDomainTextChanged(text: String) {
            val newDomain = if (text.isEmpty()) {
                TextInput(value = text, state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateDomain(text)
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
            model.newDomain = newDomain
            scene.setNewDomainState(newDomain.state)
            scene.enableNextButton(model.newDomain.state is FormInputState.Valid)
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

        override fun onGeneralCancelButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogCriptextPlus -> {
                    if(result.type is DialogType.CriptextPlus){
                        host.dismissCriptextPlusDialog()
                    }
                }
            }
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
            host.finishScene()
        }
    }

    private val dataSourceListener = { result: CustomDomainEntryResult ->
        when (result) {
            is CustomDomainEntryResult.CheckDomainAvailability -> onCheckDomainAvailability(result)
            is CustomDomainEntryResult.RegisterDomain -> onRegisterDomain(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(uiObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return handleActivityMessage(activityMessage)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        return when (activityMessage) {
            is ActivityMessage.ShowUIMessage -> {
                scene.showMessage(activityMessage.message)
                true
            }
            is ActivityMessage.IsNotPlus -> {
                if(!storage.getBool(KeyValueStorage.StringKey.HasBeenAskedPlusDomains, false)) {
                    storage.putBool(KeyValueStorage.StringKey.HasBeenAskedPlusDomains, true)
                    host.showCriptextPlusDialog(
                            dialogData = DialogData.DialogCriptextPlusData(
                                    image = R.drawable.img_domain,
                                    title = UIMessage(R.string.plus_dialog_custom_domains_title),
                                    type = DialogType.CriptextPlus(),
                                    message = UIMessage(R.string.you_need_plus_message_custom_domains)
                            ),
                            uiObserver = uiObserver
                    )
                }
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onCheckDomainAvailability(result: CustomDomainEntryResult.CheckDomainAvailability){
        when(result){
            is CustomDomainEntryResult.CheckDomainAvailability.Success -> {
                dataSource.submitRequest(CustomDomainEntryRequest.RegisterDomain(result.domain))
            }
            is CustomDomainEntryResult.CheckDomainAvailability.Failure -> {
                scene.progressNextButton(false)
                scene.enableNextButton(false)
                scene.setDomainError(result.message)
            }
        }
    }

    private fun onRegisterDomain(result: CustomDomainEntryResult.RegisterDomain){
        scene.progressNextButton(false)
        when(result){
            is CustomDomainEntryResult.RegisterDomain.Success -> {
                host.goToScene(
                        params = DomainConfigurationParams(result.domain),
                        activityMessage = null,
                        keep = false
                )
            }
            is CustomDomainEntryResult.RegisterDomain.Failure -> {
                scene.enableNextButton(false)
                scene.setDomainError(result.message)
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
                        keep = false,
                        deletePastIntents = true
                )
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

                host.goToScene(
                        params = MailboxParams(),
                        activityMessage = null,
                        keep = false, deletePastIntents = true
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
}