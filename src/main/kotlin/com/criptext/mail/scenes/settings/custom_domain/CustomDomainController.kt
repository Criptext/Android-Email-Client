package com.criptext.mail.scenes.settings.custom_domain

import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.DomainListItemListener
import com.criptext.mail.scenes.settings.custom_domain.data.*
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
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton

class CustomDomainController(
        private val model: CustomDomainModel,
        private val scene: CustomDomainScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: CustomDomainDataSource)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val domainWrapperListController = DomainWrapperListController(model, scene.getDomainListView())

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.SyncAccept -> onSyncAccept(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
            is GeneralResult.GetUserSettings -> onGetUserSettings(result)
            is GeneralResult.UpdateLocalDomainAndAliasData -> onUpdateLocalDomainAndAliasData(result)
        }
    }

    private val uiObserver = object: CustomDomainUIObserver{
        override fun onValidateDomainPressed(domainName: String, position: Int) {
            val domainItem = model.domains.getOrNull(position) ?: return
            val customDomain = CustomDomain(
                    id = domainItem.id,
                    rowId = domainItem.rowId,
                    name = domainItem.name,
                    validated = domainItem.validated,
                    accountId = domainItem.accountId
            )
            host.goToScene(DomainConfigurationParams(customDomain), activityMessage = ActivityMessage.NonValidatedDomainFound(customDomain), keep = true)
        }

        override fun onRemoveDomainConfirmed(domainName: String, position: Int) {
            dataSource.submitRequest(CustomDomainRequest.DeleteDomain(domainName, position))
        }

        override fun onRemoveDeviceCancel() {

        }

        override fun onRemoveDomain(domainName: String, position: Int) {
            scene.showRemoveDomainDialog(domainName, position)
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
                        host.finishScene()
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
                is DialogResult.DialogCriptextPlus -> {
                    if(result.type is DialogType.CriptextPlus){
                        host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("criptext-billing", activeAccount.jwt))
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

    private val dataSourceListener = { result: CustomDomainResult ->
        when(result) {
            is CustomDomainResult.DeleteDomain -> onDomainDeleted(result)
            is CustomDomainResult.LoadDomain -> onDomainsLoaded(result)
        }
    }

    private val onDevicesListItemListener: DomainListItemListener = object: DomainListItemListener {
        override fun onCustomDomainTrashClicked(domain: DomainItem, position: Int): Boolean {
            uiObserver.onRemoveDomain(domain.name, position)
            return true
        }

        override fun onCustomDomainValidateClicked(domain: DomainItem, position: Int) {
            uiObserver.onRemoveDomain(domain.name, position)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(uiObserver, keyboardManager, model, onDevicesListItemListener)
        if(activityMessage != null && activityMessage is ActivityMessage.DomainRegistered){
            domainWrapperListController.addAll(listOf(DomainItem(activityMessage.customDomain, listOf())))
            scene.showMessage(UIMessage(R.string.domain_setup_complete))
        } else {
            scene.showProgressBar(true)
            generalDataSource.submitRequest(GeneralRequest.GetUserSettings())
        }
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onDomainDeleted(result: CustomDomainResult.DeleteDomain){
        when(result){
            is CustomDomainResult.DeleteDomain.Success -> {
                domainWrapperListController.remove(result.position)
                host.exitToScene(CustomDomainEntryParams(), ActivityMessage.ShowUIMessage(UIMessage(R.string.domain_delete_complete)), false)
            }
            is CustomDomainResult.DeleteDomain.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private fun onDomainsLoaded(result: CustomDomainResult.LoadDomain){
        scene.showProgressBar(false)
        when(result){
            is CustomDomainResult.LoadDomain.Success -> {
                if (result.domains.isNotEmpty())
                    domainWrapperListController.addAll(result.domains.map { DomainItem(it, listOf()) })
            }
            is CustomDomainResult.LoadDomain.Failure -> {
                host.exitToScene(CustomDomainEntryParams(), null, true)
            }
        }
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

    private fun onGetUserSettings(result: GeneralResult.GetUserSettings){
        when(result) {
            is GeneralResult.GetUserSettings.Success -> {
                generalDataSource.submitRequest(
                        GeneralRequest.UpdateLocalDomainAndAliasData(
                                result.userSettings.customDomains,
                                result.userSettings.aliases
                        )
                )
                if(result.userSettings.customerType == AccountTypes.STANDARD){
                    if(activeAccount.type == AccountTypes.STANDARD){
                        host.showCriptextPlusDialog(
                                dialogData = DialogData.DialogCriptextPlusData(
                                        image = R.drawable.img_domain,
                                        title = UIMessage(R.string.you_need_plus_title),
                                        type = DialogType.CriptextPlus(),
                                        message = UIMessage(R.string.you_need_plus_message_custom_domains)
                                ),
                                uiObserver = uiObserver
                        )
                    }
                }
            }
            is GeneralResult.GetUserSettings.Failure -> {
                dataSource.submitRequest(CustomDomainRequest.LoadDomain())
            }
        }
    }

    private fun onUpdateLocalDomainAndAliasData(result: GeneralResult.UpdateLocalDomainAndAliasData){
        when(result) {
            is GeneralResult.UpdateLocalDomainAndAliasData.Success -> {
                dataSource.submitRequest(CustomDomainRequest.LoadDomain())
            }
            is GeneralResult.UpdateLocalDomainAndAliasData.Failure -> {
                dataSource.submitRequest(CustomDomainRequest.LoadDomain())
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