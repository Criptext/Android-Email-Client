package com.criptext.mail.scenes.settings.custom_domain

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.*
import com.criptext.mail.scenes.settings.DomainListItemListener
import com.criptext.mail.scenes.settings.custom_domain.data.*
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.scenes.webview.WebViewSceneController
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
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton
import java.util.*

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
    : SceneController(host, activeAccount, storage){

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

    private val uiObserver = object: CustomDomainUIObserver(generalDataSource, host) {
        override fun onValidateDomainPressed(domainName: String, position: Int) {
            val domainItem = model.domains.getOrNull(position) ?: return
            val customDomain = CustomDomain(
                    id = domainItem.id,
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
                is DialogResult.DialogCriptextPlus -> {
                    if(result.type is DialogType.CriptextPlus){
                        host.goToScene(
                                params = WebViewParams(
                                        url = "${WebViewSceneController.ADMIN_URL}?token=${activeAccount.jwt}&lang=${Locale.getDefault().language}"
                                ),
                                activityMessage = null,
                                keep = true,
                                animationData = TransitionAnimationData(
                                        forceAnimation = true,
                                        enterAnim = R.anim.slide_in_up,
                                        exitAnim = R.anim.stay
                                )
                        )
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
            host.goToScene(DomainConfigurationParams(CustomDomain(
                    id = domain.id,
                    accountId = domain.accountId,
                    validated = domain.validated,
                    name = domain.name
            )), true)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(uiObserver, keyboardManager, model, onDevicesListItemListener)
        if(activityMessage != null && activityMessage is ActivityMessage.DomainRegistered){
            domainWrapperListController.replaceAll(listOf(DomainItem(activityMessage.customDomain, listOf())))
            scene.showMessage(UIMessage(R.string.domain_setup_complete))
        } else {
            if(model.domains.isEmpty()) {
                scene.showProgressBar(true)
                generalDataSource.submitRequest(GeneralRequest.GetUserSettings())
            }
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
                host.goToScene(
                        params = CustomDomainEntryParams(),
                        activityMessage = ActivityMessage.ShowUIMessage(UIMessage(R.string.domain_delete_complete)),
                        keep = false
                )
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
                if(AccountUtils.canJoinPlus(model.accountType)
                        && !storage.getBool(KeyValueStorage.StringKey.HasBeenAskedPlusDomains, false)){
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
                if (result.domains.isNotEmpty())
                    domainWrapperListController.replaceAll(result.domains.map { DomainItem(it, listOf()) })
            }
            is CustomDomainResult.LoadDomain.Failure -> {
                val activityMessage = if(AccountUtils.canJoinPlus(model.accountType)) {
                    ActivityMessage.IsNotPlus()
                } else null
                host.goToScene(
                        params = CustomDomainEntryParams(),
                        activityMessage = activityMessage,
                        animationData = TransitionAnimationData(
                                forceAnimation = true,
                                enterAnim = 0,
                                exitAnim = 0
                        ), keep = false
                )
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

    private fun onGetUserSettings(result: GeneralResult.GetUserSettings){
        when(result) {
            is GeneralResult.GetUserSettings.Success -> {
                generalDataSource.submitRequest(
                        GeneralRequest.UpdateLocalDomainAndAliasData(
                                result.userSettings.customDomains,
                                result.userSettings.aliases
                        )
                )
                model.accountType = result.userSettings.customerType
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