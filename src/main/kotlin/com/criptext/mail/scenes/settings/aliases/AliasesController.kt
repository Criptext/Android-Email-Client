package com.criptext.mail.scenes.settings.aliases

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.AliasListItemListener
import com.criptext.mail.scenes.settings.aliases.data.*
import com.criptext.mail.scenes.settings.custom_domain.data.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.data.UserDataWriter
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton

class AliasesController(
        private val model: AliasesModel,
        private val scene: AliasesScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: AliasesDataSource)
    : SceneController(){

    override val menuResourceId: Int? = null
    private val criptextAliasWrapperListController = CriptextAliasWrapperListController(model, scene.getCriptextAliasesListView())
    private val customAliasWrapperListController = CustomDomainAliasWrapperListController(model, scene.getCriptextAliasesListView())

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

    private val uiObserver = object: AliasesUIObserver{
        override fun onAddAliasSpinnerChangeSelection(domain: String) {

        }

        override fun onAddAliasTextChanged(newAlias: String) {
            if(newAlias.isNotEmpty()) {
                val input = AccountDataValidator.validateUsernameOnly(newAlias)
                when(input){
                    is FormData.Valid -> {
                        scene.setAddAliasDialogError(null)
                    }
                    is FormData.Error -> {
                        scene.setAddAliasDialogError(input.message)
                    }
                }
            }
        }

        override fun onAddAliasOkPressed(newAlias: String, domain: String) {
            scene.addAliasDialogToggleLoad(true)
            val trueDomain = if(domain.removePrefix("@") == Contact.mainDomain) null
            else domain.removePrefix("@")
            dataSource.submitRequest(AliasesRequest.AddAlias(newAlias, trueDomain))
        }

        override fun onAddAliasButtonPressed() {
            val domainList = mutableListOf<String>()
            if(model.domains.map { it.name }.isNotEmpty()) {
                domainList.add("@${Contact.mainDomain}")
                domainList.addAll(model.domains.map { "@${it.name}" })
            }
            scene.showAddAliasDialog(domainList)
        }

        override fun onRemoveAliasConfirmed(aliasName: String, domainName: String?, position: Int) {
            dataSource.submitRequest(AliasesRequest.DeleteAlias(aliasName, domainName, position))
        }

        override fun onRemoveAliasCancel() {

        }

        override fun onRemoveAlias(aliasName: String, domainName: String?, position: Int) {
            scene.showRemoveAliasDialog(aliasName, domainName, position)
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

    private val dataSourceListener = { result: AliasesResult ->
        when(result) {
            is AliasesResult.AddAlias -> onAliasAdded(result)
            is AliasesResult.DeleteAlias -> onAliasDeleted(result)
            is AliasesResult.LoadAliases -> onAliasesLoaded(result)
            is AliasesResult.EnableAlias -> onAliasEnabled(result)
        }
    }

    private val onAliasesListItemListener: AliasListItemListener = object: AliasListItemListener {
        override fun onAliasActiveSwitched(alias: AliasItem, position: Int, enabled: Boolean) {
            dataSource.submitRequest(AliasesRequest.EnableAlias(alias.name, alias.domain, enabled, position))
        }

        override fun onAliasTrashClicked(alias: AliasItem, position: Int): Boolean {
            uiObserver.onRemoveAlias(alias.name, alias.domain, position)
            return true
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(uiObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        scene.showProgressBar(true)
        generalDataSource.submitRequest(GeneralRequest.GetUserSettings())
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        return false
    }

    private fun onAliasAdded(result: AliasesResult.AddAlias){
        scene.addAliasDialogToggleLoad(false)
        when(result){
            is AliasesResult.AddAlias.Success -> {
                scene.addAliasDialogDismiss()
                val domain = result.alias.domain
                if(domain == null){
                    criptextAliasWrapperListController.add(AliasItem(result.alias))
                } else {
                    customAliasWrapperListController.add(domain, AliasItem(result.alias))
                }
                scene.setupAliasesFromModel(model, onAliasesListItemListener)
                scene.showMessage(UIMessage(R.string.aliases_create_added))
            }
            is AliasesResult.AddAlias.Failure -> {
                scene.setAddAliasDialogError(result.message)
            }
        }
    }

    private fun onAliasDeleted(result: AliasesResult.DeleteAlias){
        when(result){
            is AliasesResult.DeleteAlias.Success -> {
                if(result.domain != null){
                    customAliasWrapperListController.remove(result.position, result.domain)
                    scene.setupAliasesFromModel(model, onAliasesListItemListener)
                    scene.showMessage(UIMessage(R.string.aliases_delete_success, arrayOf("${result.aliasName}@${result.domain}")))
                } else {
                    criptextAliasWrapperListController.remove(result.position)
                    scene.showMessage(UIMessage(R.string.aliases_delete_success, arrayOf("${result.aliasName}@${Contact.mainDomain}")))
                }
            }
            is AliasesResult.DeleteAlias.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private fun onAliasesLoaded(result: AliasesResult.LoadAliases){
        scene.showProgressBar(false)
        when(result){
            is AliasesResult.LoadAliases.Success -> {
                val criptextAliases = result.aliases.filter { it.domain == null }
                val customAliases = result.aliases.filter { it.domain != null }
                model.domains = ArrayList(result.domains.map { domain ->
                    DomainItem(domain, customAliases.filter { it.domain == domain.name })
                })
                model.criptextAliases = ArrayList(criptextAliases.map { AliasItem(it) })
                if(result.aliases.isNotEmpty()) {
                    scene.setupAliasesFromModel(model, onAliasesListItemListener)
                }
            }
            is AliasesResult.LoadAliases.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    private fun onAliasEnabled(result: AliasesResult.EnableAlias){
        when(result){
            is AliasesResult.EnableAlias.Success -> {
                if(result.domain != null){
                    customAliasWrapperListController.updateActive(result.position, result.domain, result.enable)
                } else {
                    criptextAliasWrapperListController.updateActive(result.position, result.enable)
                }
            }
            is AliasesResult.EnableAlias.Failure -> {
                if(result.domain != null){
                    customAliasWrapperListController.updateActive(result.position, result.domain, !result.enable)
                } else {
                    criptextAliasWrapperListController.updateActive(result.position, !result.enable)
                }
                scene.showMessage(result.message)
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
            }
            is GeneralResult.GetUserSettings.Failure -> {
                dataSource.submitRequest(AliasesRequest.LoadAliases())
            }
        }
    }

    private fun onUpdateLocalDomainAndAliasData(result: GeneralResult.UpdateLocalDomainAndAliasData){
        when(result) {
            is GeneralResult.UpdateLocalDomainAndAliasData.Success -> {
                dataSource.submitRequest(AliasesRequest.LoadAliases())
            }
            is GeneralResult.UpdateLocalDomainAndAliasData.Failure -> {
                dataSource.submitRequest(AliasesRequest.LoadAliases())
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

    companion object {
        const val MAX_ALIAS_COUNT = 3
    }
}