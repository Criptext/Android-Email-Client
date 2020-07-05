package com.criptext.mail.scenes.settings.replyto

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
import com.criptext.mail.scenes.params.ProfileParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.settings.replyto.data.ReplyToDataSource
import com.criptext.mail.scenes.settings.replyto.data.ReplyToRequest
import com.criptext.mail.scenes.settings.replyto.data.ReplyToResult
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import com.criptext.mail.websocket.WebSocketSingleton

class ReplyToController(
        private val model: ReplyToModel,
        private val scene: ReplyToScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private var activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private var websocketEvents: WebSocketEventPublisher,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: ReplyToDataSource)
    : SceneController(host, activeAccount, storage){

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.DeviceRemoved -> onDeviceRemovedRemotely(result)
            is GeneralResult.ConfirmPassword -> onPasswordChangedRemotely(result)
            is GeneralResult.LinkAccept -> onLinkAccept(result)
            is GeneralResult.ChangeToNextAccount -> onChangeToNextAccount(result)
        }
    }

    private val dataSourceListener = { result: ReplyToResult ->
        when (result) {
            is ReplyToResult.SetReplyToEmail -> onReplyEmailChanged(result)
        }
    }

    private val replyToUIObserver = object: ReplyToUIObserver(generalDataSource, host) {

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

        override fun onSnackbarClicked() {

        }

        override fun onTurnOffReplyTo() {
            model.newReplyToEmail = ""
            scene.clearTextBox()
            dataSource.submitRequest(ReplyToRequest.SetReplyToEmail(model.newReplyToEmail, false))
        }

        override fun onRecoveryChangeButonPressed() {
            scene.disableSaveButton()
            dataSource.submitRequest(ReplyToRequest.SetReplyToEmail(model.newReplyToEmail, true))
        }

        override fun onRecoveryEmailChanged(text: String) {
            model.newReplyToEmail = text
            val userInput = AccountDataValidator.validateEmailAddress(model.newReplyToEmail)
            when (userInput) {
                is FormData.Valid -> {
                    if (!text.isEmpty() && text != model.userData.replyToEmail) {
                        scene.setEmailError(null)
                        scene.enableSaveButton()
                    } else {
                        scene.disableSaveButton()
                    }
                }
                is FormData.Error -> {
                    scene.disableSaveButton()
                    scene.setEmailError(userInput.message)
                }
            }
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            val message = if(model.comesFromMailbox) ActivityMessage.ComesFromMailbox() else null
            host.goToScene(
                    params = ProfileParams(false),
                    activityMessage = message,
                    keep = false
            )
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        if(activityMessage is ActivityMessage.ComesFromMailbox)
            model.comesFromMailbox = true
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        websocketEvents.setListener(webSocketEventListener)
        scene.attachView(replyToUIObserver, model.userData.replyToEmail ?: "", keyboardManager)
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
                dataSource.activeAccount = activeAccount
                val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
                websocketEvents = if(jwts.isNotEmpty())
                    WebSocketSingleton.getInstance(jwts)
                else
                    WebSocketSingleton.getInstance(activeAccount.jwt)

                websocketEvents.setListener(webSocketEventListener)

                scene.dismissAccountSuspendedDialog()

                scene.showMessage(UIMessage(R.string.snack_bar_active_account, arrayOf(activeAccount.userEmail)))
            }
        }
    }

    private fun onReplyEmailChanged(result: ReplyToResult.SetReplyToEmail){
        when(result) {
            is ReplyToResult.SetReplyToEmail.Success -> {
                if(result.enabled) {
                    model.userData.replyToEmail = result.replyToEmail
                    scene.showMessage(UIMessage(R.string.reply_to_email_has_changed))
                }else {
                    model.userData.replyToEmail = null
                    scene.enableSaveButton()
                    scene.showMessage(UIMessage(R.string.reply_to_email_removed))
                }
            }
            is ReplyToResult.SetReplyToEmail.Failure -> {
                scene.enableSaveButton()
                scene.showMessage(result.message)
            }
            is ReplyToResult.SetReplyToEmail.Forbidden -> {
                host.showConfirmPasswordDialog(replyToUIObserver)
            }
            is ReplyToResult.SetReplyToEmail.EnterpriseSuspended-> {
                showSuspendedAccountDialog()
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

    override fun onBackPressed(): Boolean {
        replyToUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    private fun showSuspendedAccountDialog(){
        val jwtList = storage.getString(KeyValueStorage.StringKey.JWTS, "").split(",").map { it.trim() }
        val dialogType = if(jwtList.isNotEmpty() && jwtList.size > 1) DialogType.SwitchAccount()
        else DialogType.SignIn()
        scene.showAccountSuspendedDialog(replyToUIObserver, activeAccount.userEmail, dialogType)
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
                host.showConfirmPasswordDialog(replyToUIObserver)
            })
        }

        override fun onDeviceRemoved() {
            generalDataSource.submitRequest(GeneralRequest.DeviceRemoved(false))
        }

        override fun onError(uiMessage: UIMessage) {
            scene.showMessage(uiMessage)
        }
    }
}