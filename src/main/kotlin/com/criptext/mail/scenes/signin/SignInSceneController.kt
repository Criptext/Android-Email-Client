package com.criptext.mail.scenes.signin

import com.criptext.mail.BuildConfig
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ContactDomainCheckData
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignUpParams
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordController
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.data.*
import com.criptext.mail.scenes.signin.holders.SignInLayoutState
import com.criptext.mail.utils.*
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState
import com.criptext.mail.websocket.CriptextWebSocketFactory
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher

/**
 * Created by sebas on 2/15/18.
 */

class SignInSceneController(
        private val webSocketFactory: CriptextWebSocketFactory,
        private val model: SignInSceneModel,
        private val scene: SignInScene,
        private val host: IHostActivity,
        private val storage: KeyValueStorage,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: SignInDataSource,
        private val keyboard: KeyboardManager): SceneController() {

    override val menuResourceId: Int? = R.menu.menu_remove_device_holder

    private var tempWebSocket: WebSocketEventPublisher? = null
    private var webSocket: WebSocketEventPublisher? = null

    private val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    private val dataSourceListener = { result: SignInResult ->
        when (result) {
            is SignInResult.AuthenticateUser -> onUserAuthenticated(result)
            is SignInResult.CheckUsernameAvailability -> onCheckUsernameAvailability(result)
            is SignInResult.LinkBegin -> onLinkBegin(result)
            is SignInResult.LinkAuth -> onLinkAuth(result)
            is SignInResult.CreateSessionFromLink -> onCreateSessionFromLink(result)
            is SignInResult.LinkDataReady -> onLinkDataReady(result)
            is SignInResult.LinkData -> onLinkData(result)
            is SignInResult.LinkStatus -> onLinkStatus(result)
            is SignInResult.FindDevices -> onFindDevices(result)
            is SignInResult.RemoveDevices -> onRemoveDevices(result)
            is SignInResult.RecoveryCode -> onGenerateRecoveryCode(result)
        }
    }

    private val generalDataSourceListener = { result: GeneralResult ->
        when (result) {
            is GeneralResult.ResetPassword -> onForgotPassword(result)
            is GeneralResult.DeviceRemoved -> onDeviceRemoved(result)
        }
    }

    private var deviceWrapperListController: DeviceWrapperListController? = null

    private fun cancelLink(canceledByMe: Boolean = true){
        stopTempWebSocket()
        stopWebSocket()
        host.getHandler()?.removeCallbacks(null)
        model.linkDeviceState = LinkDeviceState.Begin()
        val activeAccount = ActiveAccount.loadFromStorage(storage)
        if(activeAccount == null){
            val currentState = model.state as? SignInLayoutState.LoginValidation
            if(currentState != null) {
                model.state = SignInLayoutState.Start(currentState.username, firstTime = false)
                resetLayout()
                if(canceledByMe)
                    generalDataSource.submitRequest(GeneralRequest.LinkCancel(currentState.username, currentState.domain, model.ephemeralJwt, null))
            }
        }else{
            if(canceledByMe)
                generalDataSource.submitRequest(GeneralRequest.LinkCancel(activeAccount.recipientId, activeAccount.domain, activeAccount.jwt, activeAccount.deviceId))
            host.exitToScene(MailboxParams(), null, false, true)
        }
    }

    private fun onAuthenticationFailed(errorMessage: UIMessage) {
        scene.showError(errorMessage)

        val currentState = model.state
        if (currentState is SignInLayoutState.InputPassword) {
            model.state = currentState.copy(password = "",
                    buttonState = ProgressButtonState.disabled)
            scene.resetInput()
        } else if(currentState is SignInLayoutState.LoginValidation) {
            if(model.needToRemoveDevices){
                model.realSecurePassword = null
                model.needToRemoveDevices = false
            }
            model.state = SignInLayoutState.InputPassword(
                    username = currentState.username,
                    domain = currentState.domain,
                    password = "",
                    buttonState = ProgressButtonState.disabled,
                    hasTwoFA = model.hasTwoFA
            )
            scene.initLayout(model, uiObserver)
        }
    }

    private fun onForgotPassword(result: GeneralResult.ResetPassword){
        if(model.state is SignInLayoutState.InputPassword) {
            scene.toggleForgotPasswordClickable(true)
            when (result) {
                is GeneralResult.ResetPassword.Success -> {
                    scene.showResetPasswordDialog(result.email)
                }
                is GeneralResult.ResetPassword.Failure -> scene.showError(result.message)
            }
        }
    }

    private fun onDeviceRemoved(result: GeneralResult.DeviceRemoved){
        when(result){
            is GeneralResult.DeviceRemoved.Success -> {
                val currentState = model.state as SignInLayoutState.Connection
                model.state = SignInLayoutState.Start(currentState.username, firstTime = false)
                resetLayout()
            }
        }
    }

    private fun onCheckUsernameAvailability(result: SignInResult.CheckUsernameAvailability) {
        when (result) {
            is SignInResult.CheckUsernameAvailability.Success -> {
                if(result.userExists) {
                    keyboard.hideKeyboard()
                    val oldAccounts = AccountUtils.getLastLoggedAccounts(storage)
                    if(oldAccounts.isNotEmpty() && result.username.plus("@${result.domain}") !in oldAccounts)
                        scene.showSignInWarningDialog(
                                oldAccountName = oldAccounts.joinToString {
                                    if(AccountDataValidator.validateEmailAddress(it) is FormData.Valid) it
                                    else it.plus(EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX)
                                },
                                newUserName = result.username,
                                domain = result.domain
                        )
                    else {
                        //LINK DEVICE FEATURE
                        model.state = SignInLayoutState.LoginValidation(username = result.username,
                                domain = result.domain,
                                hasTwoFA = model.hasTwoFA)
                        dataSource.submitRequest(SignInRequest.LinkBegin(result.username, result.domain))
                    }
                }
                else{
                    scene.drawInputError(UIMessage(R.string.username_doesnt_exist))
                }
            }
            is SignInResult.CheckUsernameAvailability.Failure -> scene.showError(result.message)
        }
        scene.setSubmitButtonState(ProgressButtonState.enabled)
    }

    private fun onLinkBegin(result: SignInResult.LinkBegin) {
        when (result) {
            is SignInResult.LinkBegin.Success -> {

                val currentState = model.state as SignInLayoutState.LoginValidation
                model.ephemeralJwt = result.ephemeralJwt
                model.hasTwoFA = result.hasTwoFA
                if(model.hasTwoFA){
                    if(model.realSecurePassword != null){
                        dataSource.submitRequest(SignInRequest.LinkAuth(currentState.username,
                                model.ephemeralJwt, currentState.domain, model.realSecurePassword))
                    } else {
                        onAcceptPasswordLogin(currentState.username, currentState.domain)
                    }
                }else{
                    scene.initLayout(model, uiObserver)
                    scene.toggleResendClickable(true)
                    handleNewTemporalWebSocket()
                    dataSource.submitRequest(SignInRequest.LinkAuth(currentState.username,
                            model.ephemeralJwt, currentState.domain))
                }
            }
            is SignInResult.LinkBegin.Failure -> returnToStart(result.message)
            is SignInResult.LinkBegin.NeedToRemoveDevices -> {
                model.needToRemoveDevices = true
                val currentState = model.state as SignInLayoutState.LoginValidation
                onAcceptPasswordLogin(currentState.username, currentState.domain)
            }
            is SignInResult.LinkBegin.NoDevicesAvailable -> {
                val currentState = model.state as SignInLayoutState.LoginValidation
                onAcceptPasswordLogin(currentState.username, currentState.domain)
            }
        }
    }

    private fun returnToStart(message: UIMessage){
        val currentState = model.state as SignInLayoutState.LoginValidation
        model.state = SignInLayoutState.Start(currentState.username, false)
        scene.initLayout(model, uiObserver)
        scene.showError(message)
    }

    private fun onLinkAuth(result: SignInResult.LinkAuth) {
        if(model.state is SignInLayoutState.Start) return
        when (result) {
            is SignInResult.LinkAuth.Success -> {
                if(model.linkDeviceState is LinkDeviceState.Accepted) return
                handleNewTemporalWebSocket()
                model.linkDeviceState = LinkDeviceState.Auth()
                host.postDelay(Runnable{
                    if(model.retryTimeLinkStatus < RETRY_TIMES_DEFAULT) {
                        if (model.linkDeviceState is LinkDeviceState.Auth)
                            dataSource.submitRequest(SignInRequest.LinkStatus(model.ephemeralJwt))
                        model.retryTimeLinkStatus++
                    }
                }, RETRY_TIME_DEFAULT)

            }
            is SignInResult.LinkAuth.Failure -> {
                if(model.hasTwoFA){
                    val resultData = SignInResult.AuthenticateUser.Failure(result.message,
                            result.exception)
                    onAuthenticationFailed(resultData.message)
                }else {
                    scene.showError(UIMessage(R.string.server_error_exception))
                }
            }
        }
    }

    private fun onCreateSessionFromLink(result: SignInResult.CreateSessionFromLink) {
        if(model.state is SignInLayoutState.Start) return
        when (result) {
            is SignInResult.CreateSessionFromLink.Success -> {
                scene.setLinkProgress(UIMessage(R.string.waiting_for_mailbox), WAITING_FOR_MAILBOX_PERCENTAGE)
                model.activeAccount = result.activeAccount
                stopTempWebSocket()
                handleNewWebSocket()
                if(model.retryTimeLinkDataReady < RETRY_TIMES_DATA_READY) {
                    if (model.linkDeviceState !is LinkDeviceState.WaitingForDownload) {
                        host.postDelay(Runnable {
                            dataSource.submitRequest(SignInRequest.LinkDataReady())
                        }, RETRY_TIME_DEFAULT)
                    }
                    model.retryTimeLinkDataReady++
                }
            }
            is SignInResult.CreateSessionFromLink.Failure -> {
                scene.showSyncRetryDialog(result)
            }
        }
    }

    private fun onLinkDataReady(result: SignInResult.LinkDataReady) {
        if(model.state is SignInLayoutState.Start) return
        when (result) {
            is SignInResult.LinkDataReady.Success -> {
                if(model.linkDeviceState !is LinkDeviceState.WaitingForDownload) {
                    model.linkDeviceState = LinkDeviceState.WaitingForDownload()
                    model.key = result.key
                    model.dataAddress = result.dataAddress
                    dataSource.submitRequest(SignInRequest.LinkData(model.key, model.dataAddress,
                            model.authorizerId))
                }
            }
            is SignInResult.LinkDataReady.Failure -> {
                if(model.retryTimeLinkDataReady < RETRY_TIMES_DATA_READY) {
                    if (model.linkDeviceState !is LinkDeviceState.WaitingForDownload){
                        host.postDelay(Runnable{
                            dataSource.submitRequest(SignInRequest.LinkDataReady())
                        }, RETRY_TIME_DATA_READY)
                        model.retryTimeLinkDataReady++
                    }
                } else {
                    scene.showSyncRetryDialog(result)
                }
            }
        }
    }

    private fun onLinkData(result: SignInResult.LinkData) {
        if(model.state is SignInLayoutState.Start) return
        when (result) {
            is SignInResult.LinkData.Success -> {
                scene.setLinkProgress(UIMessage(R.string.sync_complete), SYNC_COMPLETE_PERCENTAGE)
                scene.startLinkSucceedAnimation()
            }
            is SignInResult.LinkData.Progress -> {
                scene.setLinkProgress(result.message, result.progress)
            }
            is SignInResult.LinkData.Failure -> {
                scene.showSyncRetryDialog(result)
            }
        }
    }

    private fun onLinkStatus(result: SignInResult.LinkStatus) {
        if(model.state is SignInLayoutState.Start) return
        when (result) {
            is SignInResult.LinkStatus.Success -> {
                if(model.linkDeviceState is LinkDeviceState.Auth) {
                    model.linkDeviceState = LinkDeviceState.Accepted()
                    val currentState = model.state as SignInLayoutState.LoginValidation
                    model.name = result.linkStatusData.name
                    model.randomId = result.linkStatusData.deviceId
                    model.authorizerId = result.linkStatusData.authorizerId
                    model.authorizerType = result.linkStatusData.authorizerType
                    model.state = SignInLayoutState.Connection(currentState.username, currentState.domain, model.authorizerType)
                    scene.initLayout(model, uiObserver)
                    scene.setLinkProgress(UIMessage(R.string.sending_keys), SENDING_KEYS_PERCENTAGE)
                    dataSource.submitRequest(SignInRequest.CreateSessionFromLink(name = model.name,
                            username = currentState.username,
                            domain = currentState.domain,
                            randomId = model.randomId, ephemeralJwt = model.ephemeralJwt,
                            isMultiple = model.isMultiple))
                }

            }
            is SignInResult.LinkStatus.Waiting -> {
                host.postDelay(Runnable{
                    if(model.retryTimeLinkStatus < RETRY_TIMES_DEFAULT) {
                        if (model.linkDeviceState is LinkDeviceState.Auth)
                            dataSource.submitRequest(SignInRequest.LinkStatus(model.ephemeralJwt))
                        model.retryTimeLinkStatus++
                    }
                }, RETRY_TIME_DEFAULT)
            }
            is SignInResult.LinkStatus.Denied -> {
                if(model.linkDeviceState !is LinkDeviceState.Denied) {
                    model.linkDeviceState = LinkDeviceState.Denied()
                    val currentState = model.state as SignInLayoutState.LoginValidation
                    model.state = SignInLayoutState.DeniedValidation(currentState.username, currentState.domain)
                    scene.initLayout(model, uiObserver)
                }
            }
        }
    }

    private fun onFindDevices(result: SignInResult.FindDevices){
        when (result) {
            is SignInResult.FindDevices.Success -> {
                model.temporalJWT = result.token
                model.devices = result.devices
                val currentState = model.state as SignInLayoutState.InputPassword
                model.state = SignInLayoutState.RemoveDevices(
                        username = currentState.username,
                        buttonState = ProgressButtonState.disabled,
                        domain = currentState.domain,
                        devices = model.devices,
                        password = currentState.password)
                scene.initLayout(model, uiObserver, onDevicesListItemListener)
            }
            is SignInResult.FindDevices.Failure -> {
                onAuthenticationFailed(result.message)
            }
        }
    }

    private fun onRemoveDevices(result: SignInResult.RemoveDevices){
        when (result) {
            is SignInResult.RemoveDevices.Success -> {
                uiObserver.onXPressed()
                deviceWrapperListController?.remove(result.deviceIds)
                if(model.devices.size >= DeviceItem.MAX_ALLOWED_DEVICES){
                    scene.showDeviceCountRemaining(model.devices.size - (DeviceItem.MAX_ALLOWED_DEVICES - 1))
                } else {
                    val state = model.state as SignInLayoutState.RemoveDevices
                    model.state = SignInLayoutState.LoginValidation(username = state.username,
                            domain = state.domain,
                            hasTwoFA = model.hasTwoFA,
                            hasRemovedDevices = true)
                    scene.initLayout(model, uiObserver, onDevicesListItemListener)
                    model.realSecurePassword = state.password.sha256()
                    dataSource.submitRequest(SignInRequest.LinkBegin(state.username, state.domain))
                }
            }
            is SignInResult.RemoveDevices.Failure -> {
                scene.showDeviceRemovalError()
            }
        }
    }

    private fun onGenerateRecoveryCode(result: SignInResult.RecoveryCode){
        when(result){
            is SignInResult.RecoveryCode.Success -> {
                if(result.isValidate) {
                    scene.dismissRecoveryCodeDialog()
                    scene.showKeyGenerationHolder()
                } else {
                    val message = if(result.emailAddress == null) UIMessage(R.string.recovery_code_dialog_message)
                    else UIMessage(R.string.recovery_code_dialog_message_with_email, arrayOf(result.emailAddress))
                    scene.showRecoveryCode(message)
                }
            }
            is SignInResult.RecoveryCode.Failure -> {
                if(result.isValidate){
                    scene.toggleLoadRecoveryCode(false)
                    scene.showRecoveryDialogError(result.message)
                } else {
                    scene.showError(result.message)
                }
            }
        }
    }

    private fun onUserAuthenticated(result: SignInResult.AuthenticateUser) {
        when (result) {
            is SignInResult.AuthenticateUser.Success -> {
                scene.showKeyGenerationHolder()
            }
            is SignInResult.AuthenticateUser.Failure -> {
                if(result.exception is ServerErrorException
                        && result.exception.errorCode == ServerCodes.PreconditionFail){
                    val currentState = model.state as SignInLayoutState.InputPassword
                    model.state = SignInLayoutState.ChangePassword(
                            username = currentState.username,
                            buttonState = ProgressButtonState.disabled,
                            domain = currentState.domain,
                            oldPassword = currentState.password)
                    scene.initLayout(model, uiObserver)
                } else {
                    onAuthenticationFailed(result.message)
                }
            }
        }
    }

    private fun onAcceptPasswordLogin(username: String, domain: String){
        host.stopMessagesAndCallbacks()
        model.state = SignInLayoutState.InputPassword(
                username = username,
                password = "",
                buttonState = ProgressButtonState.disabled,
                domain = domain,
                hasTwoFA = model.hasTwoFA)
        scene.initLayout(model, uiObserver)
    }

    private val passwordLoginDialogListener = object : OnPasswordLoginDialogListener {

        override fun acceptPasswordLogin(username: String, domain: String) {
            onAcceptPasswordLogin(username, domain)
        }

        override fun cancelPasswordLogin() {
        }
    }

    private fun handleNewTemporalWebSocket(){
        tempWebSocket = webSocketFactory.createTemporalWebSocket(model.ephemeralJwt)
        tempWebSocket?.setListener(webSocketEventListener)
    }

    private fun handleNewWebSocket(){
        if(model.activeAccount != null) {
            webSocket = webSocketFactory.createWebSocket(model.activeAccount!!.jwt)
            webSocket?.setListener(webSocketEventListener)
        }
    }

    private fun onSignInButtonClicked(currentState: SignInLayoutState.Start) {
        val userInput = AccountDataValidator.validateUsername(currentState.username)
        when (userInput) {
            is FormData.Valid -> {
                val (recipientId, domain) = if(AccountDataValidator.validateEmailAddress(userInput.value) is FormData.Valid) {
                    val nonCriptextDomain = EmailAddressUtils.extractEmailAddressDomain(userInput.value)
                    Pair(EmailAddressUtils.extractRecipientIdFromAddress(userInput.value, nonCriptextDomain),
                            nonCriptextDomain
                    )
                } else {
                    Pair(userInput.value, Contact.mainDomain)
                }
                if(model.checkedDomains.map { it.name }.contains(domain))
                    scene.drawInputError(UIMessage(R.string.username_is_not_criptext))
                else {
                    val newRequest = SignInRequest.CheckUserAvailability(recipientId, domain)
                    dataSource.submitRequest(newRequest)
                    scene.setSubmitButtonState(ProgressButtonState.waiting)
                }
            }
            is FormData.Error ->
                scene.drawInputError(userInput.message)
        }
    }

    private fun onSignInButtonClicked(currentState: SignInLayoutState) {
        if (currentState is SignInLayoutState.InputPassword && currentState.password.isNotEmpty()) {
            val newButtonState = ProgressButtonState.waiting
            model.state = currentState.copy(buttonState = newButtonState)
            scene.setSubmitButtonState(newButtonState)

            val hashedPassword = currentState.password.sha256()
            val userData = UserData(currentState.username, currentState.domain, hashedPassword, null)
            val req = if(model.needToRemoveDevices) {
                SignInRequest.FindDevices(
                        userData = userData
                )
            } else {
                SignInRequest.AuthenticateUser(
                        userData = userData,
                        isMultiple = model.isMultiple
                )
            }

            val lastLoggedAccounts = AccountUtils.getLastLoggedAccounts(storage)
            if(!lastLoggedAccounts.contains(currentState.username))
                model.showRestoreBackupDialog = true

            dataSource.submitRequest(req)
        } else if(currentState is SignInLayoutState.ChangePassword){
            val newButtonState = ProgressButtonState.waiting
            model.state = currentState.copy(buttonState = newButtonState)
            scene.setSubmitButtonState(newButtonState)

            val hashedPassword = model.confirmPasswordText.sha256()
            val userData = UserData(currentState.username, currentState.domain, hashedPassword, currentState.oldPassword.sha256())
            val req = SignInRequest.AuthenticateUser(
                    userData = userData,
                    isMultiple = model.isMultiple
            )

            val lastLoggedAccounts = AccountUtils.getLastLoggedAccounts(storage)
            if(!lastLoggedAccounts.contains(currentState.username))
                model.showRestoreBackupDialog = true

            dataSource.submitRequest(req)
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {
            if(model.state is SignInLayoutState.Connection)
                cancelLink(false)
        }

        override fun onSyncDeviceDismiss(accountEmail: String) {
            if(model.state is SignInLayoutState.Connection)
                cancelLink(false)
        }

        override fun onAccountSuspended(accountEmail: String) {

        }

        override fun onAccountUnsuspended(accountEmail: String) {

        }

        override fun onSyncBeginRequest(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {

        }

        override fun onSyncRequestAccept(syncStatusData: SyncStatusData) {

        }

        override fun onSyncRequestDeny() {

        }

        override fun onDeviceDataUploaded(key: String, dataAddress: String, authorizerId: Int) {
            host.runOnUiThread(Runnable {
                if(model.linkDeviceState !is LinkDeviceState.WaitingForDownload) {
                    model.linkDeviceState = LinkDeviceState.WaitingForDownload()
                    model.key = key
                    model.dataAddress = dataAddress
                    model.authorizerId = authorizerId
                    dataSource.submitRequest(SignInRequest.LinkData(key, dataAddress, authorizerId))
                }
            })
        }

        override fun onDeviceLinkAuthDeny() {
            host.runOnUiThread(Runnable {
                if(model.linkDeviceState !is LinkDeviceState.Denied) {
                    model.linkDeviceState = LinkDeviceState.Denied()
                    val currentState = model.state as SignInLayoutState.LoginValidation
                    model.state = SignInLayoutState.DeniedValidation(currentState.username, currentState.domain)
                    scene.initLayout(model, uiObserver)
                }
            })
        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {
            if(model.linkDeviceState is LinkDeviceState.Auth) {
                host.runOnUiThread(Runnable {
                    model.linkDeviceState = LinkDeviceState.Accepted()
                    val currentState = model.state as SignInLayoutState.LoginValidation
                    model.name = linkStatusData.name
                    model.randomId = linkStatusData.deviceId
                    model.authorizerId = linkStatusData.authorizerId
                    model.authorizerType = linkStatusData.authorizerType
                    model.state = SignInLayoutState.Connection(currentState.username,
                            currentState.domain, model.authorizerType)
                    scene.initLayout(model, uiObserver)
                    scene.setLinkProgress(UIMessage(R.string.sending_keys), SENDING_KEYS_PERCENTAGE)
                    dataSource.submitRequest(SignInRequest.CreateSessionFromLink(name = linkStatusData.name,
                            username = currentState.username,
                            domain = currentState.domain,
                            randomId = linkStatusData.deviceId, ephemeralJwt = model.ephemeralJwt,
                            isMultiple = model.isMultiple))
                })
            }
        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {

        }

        override fun onNewEvent(recipientId: String, domain: String) {

        }

        override fun onRecoveryEmailChanged(newEmail: String) {

        }

        override fun onRecoveryEmailConfirmed() {

        }

        override fun onDeviceLocked() {

        }

        override fun onDeviceRemoved() {

        }

        override fun onError(uiMessage: UIMessage) {
            scene.showError(uiMessage)
        }
    }

    private val uiObserver = object : SignInUIObserver {
        override fun onRecoveryCodeChangeListener(newPassword: String) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogWithInput -> {
                    if(result.type is DialogType.RecoveryCode){
                        scene.toggleLoadRecoveryCode(true)
                        val currentState = model.state as SignInLayoutState.LoginValidation
                        dataSource.submitRequest(SignInRequest.RecoveryCode(currentState.username, currentState.domain, model.ephemeralJwt, model.isMultiple, result.textInput))
                    }
                }
                is DialogResult.DialogCriptextPro -> {
                    if(result.type is DialogType.CriptextPro){
                        host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("criptext-billing", model.temporalJWT))
                    }
                }
            }
        }

        override fun onOkButtonPressed(password: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCancelButtonPressed() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSnackbarClicked() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onRecoveryCodeClicked() {
            val currentState = model.state as SignInLayoutState.LoginValidation
            dataSource.submitRequest(SignInRequest.RecoveryCode(currentState.username, currentState.domain, model.ephemeralJwt, model.isMultiple))
        }

        override fun onTrashPressed(recipient: String, domain: String) {
            val checkedIndexes = Pair(mutableListOf<Int>(), mutableListOf<Int>())
            model.devices.forEachIndexed { index, deviceItem ->
                if(deviceItem.checked) {
                    checkedIndexes.first.add(deviceItem.id)
                    checkedIndexes.second.add(index)
                }
            }
            if(checkedIndexes.first.isEmpty()){
                scene.showDeviceCountRemaining(model.devices.size - (DeviceItem.MAX_ALLOWED_DEVICES - 1))
            } else {
                dataSource.submitRequest(SignInRequest.RemoveDevices(
                        userData = UserData(recipient, domain, "", null),
                        tempToken = model.temporalJWT,
                        deviceIds = checkedIndexes.first,
                        deviceIndexes = checkedIndexes.second
                ))
            }
        }

        override fun onSetupDevices(devicesListView: VirtualListView) {
            deviceWrapperListController = DeviceWrapperListController(model, devicesListView)
        }

        override fun onSignInWarningContinue(userName: String, domain: String) {
            //LINK DEVICE FEATURE
            model.state = SignInLayoutState.LoginValidation(username = userName,
                    domain = domain,
                    hasTwoFA = model.hasTwoFA)
            dataSource.submitRequest(SignInRequest.LinkBegin(userName, domain))
        }

        override fun onRetrySyncOk(result: SignInResult) {
            when(result){
                is SignInResult.CreateSessionFromLink -> {
                    val currentState = model.state as SignInLayoutState.Connection
                    scene.setLinkProgress(UIMessage(R.string.sending_keys), SENDING_KEYS_PERCENTAGE)
                    dataSource.submitRequest(SignInRequest.CreateSessionFromLink(name = model.name,
                            username = currentState.username,
                            domain = currentState.domain,
                            randomId = model.randomId, ephemeralJwt = model.ephemeralJwt,
                            isMultiple = model.isMultiple))
                }
                is SignInResult.LinkData -> {
                    scene.setLinkProgress(UIMessage(R.string.waiting_for_mailbox), WAITING_FOR_MAILBOX_PERCENTAGE)
                    dataSource.submitRequest(SignInRequest.LinkData(model.key, model.dataAddress, model.authorizerId))
                }
            }
        }

        override fun onRetrySyncCancel() {
            cancelLink()
        }

        override fun onResendDeviceLinkAuth(username: String, domain: String) {
            dataSource.submitRequest(SignInRequest.LinkAuth(username, model.ephemeralJwt,
                    domain, model.realSecurePassword))
        }

        override fun onProgressHolderFinish() {
            host.goToScene(MailboxParams(askForRestoreBackup = model.showRestoreBackupDialog), false)
        }

        override fun onBackPressed() {
            model.linkDeviceState = LinkDeviceState.Begin()
            this@SignInSceneController.onBackPressed()
        }

        override fun onXPressed() {
            deviceWrapperListController?.clearChecks()
            scene.showToolbarCount(0)
        }

        override fun onContactSupportPressed() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("help-desk", ""))
        }

        override fun onSubmitButtonClicked() {
            val state = model.state
            when (state) {
                is SignInLayoutState.Start -> onSignInButtonClicked(state)
                is SignInLayoutState.InputPassword -> {
                    if(model.hasTwoFA){
                        val currentState = model.state as SignInLayoutState.InputPassword
                        model.realSecurePassword = currentState.password.sha256()
                        model.state = SignInLayoutState.LoginValidation(currentState.username, currentState.domain, model.hasTwoFA)
                        scene.initLayout(model, this)
                        dataSource.submitRequest(SignInRequest.LinkAuth(currentState.username,
                                model.ephemeralJwt, currentState.domain, model.realSecurePassword))
                    }else{
                        onSignInButtonClicked(state)
                    }
                }
                is SignInLayoutState.ChangePassword -> {
                    keyboard.hideKeyboard()
                    onSignInButtonClicked(state)
                }
            }
        }

        override fun onForgotPasswordClick() {
            scene.toggleForgotPasswordClickable(false)
            val currentState = model.state as SignInLayoutState.InputPassword
            generalDataSource.submitRequest(GeneralRequest.ResetPassword(currentState.username,
                    currentState.domain))
        }

        override fun onCantAccessDeviceClick(){
            scene.showPasswordLoginDialog(
                    onPasswordLoginDialogListener = this@SignInSceneController.passwordLoginDialogListener)
        }

        override fun userLoginReady() {
            host.goToScene(MailboxParams(), false, true)
        }

        override fun toggleUsernameFocusState(isFocused: Boolean) {
        }

        override fun onPasswordChangeListener(newPassword: String) {
            val currentState = model.state
            if (currentState is SignInLayoutState.InputPassword) {
                val newButtonState = if (newPassword.isEmpty()) ProgressButtonState.disabled
                                     else ProgressButtonState.enabled
                model.state = currentState.copy(
                        password = newPassword,
                        buttonState = newButtonState)
                scene.setSubmitButtonState(state = newButtonState)
            } else if(currentState is SignInLayoutState.ChangePassword) {
                model.passwordText = newPassword
                if(model.confirmPasswordText.isNotEmpty())
                    checkPasswords(Pair(model.passwordText, model.confirmPasswordText))
            }
        }

        override fun onConfirmPasswordChangeListener(confirmPassword: String) {
            val currentState = model.state
            if(currentState is SignInLayoutState.ChangePassword) {
                model.confirmPasswordText = confirmPassword
                checkPasswords(Pair(model.confirmPasswordText, model.passwordText))
            }
        }

        private fun checkPasswords(passwords: Pair<String, String>) {
            if (arePasswordsMatching && passwords.first.length >= ChangePasswordController.minimumPasswordLength) {
                scene.showPasswordDialogError(null)
                model.passwordState = FormInputState.Valid()
                if (model.passwordState is FormInputState.Valid)
                    scene.toggleChangePasswordButton(true)
            } else if (arePasswordsMatching && passwords.first.isEmpty()) {
                scene.showPasswordDialogError(null)
                model.passwordState = FormInputState.Unknown()
                scene.toggleChangePasswordButton(false)
            } else if (arePasswordsMatching && passwords.first.length < ChangePasswordController.minimumPasswordLength) {
                val errorMessage = UIMessage(R.string.password_length_error)
                model.passwordState = FormInputState.Error(errorMessage)
                scene.showPasswordDialogError(errorMessage)
                scene.toggleChangePasswordButton(false)
            } else {
                val errorMessage = UIMessage(R.string.password_mismatch_error)
                model.passwordState = FormInputState.Error(errorMessage)
                scene.showPasswordDialogError(errorMessage)
                scene.toggleChangePasswordButton(false)
            }
        }

        override fun onUsernameTextChanged(newUsername: String) {
            model.state = SignInLayoutState.Start(username = newUsername, firstTime = false)
            val buttonState = if (newUsername.isNotEmpty()) ProgressButtonState.enabled
                              else ProgressButtonState.disabled
            scene.setSubmitButtonState(buttonState)
        }

        override fun onSignUpLabelClicked() {
            host.goToScene(SignUpParams(model.isMultiple), false)
        }
    }

    private fun resetLayout() {
        scene.initLayout(model, uiObserver)
    }

    private fun stopWebSocket(){
        if(webSocket != null) {
            webSocket?.clearListener(webSocketEventListener)
            webSocket?.disconnectWebSocket()
        }
    }

    private fun stopTempWebSocket(){
        if(tempWebSocket != null) {
            tempWebSocket?.clearListener(webSocketEventListener)
            tempWebSocket?.disconnectWebSocket()
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        model.checkedDomains.addAll(
                ContactDomainCheckData.KNOWN_EXTERNAL_DOMAINS.filter { !it.isCriptextDomain }
        )
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        scene.initLayout(model = model, signInUIObserver = uiObserver)
        if(activityMessage != null && activityMessage is ActivityMessage.ShowUIMessage){
            scene.showGenericOkAlert(activityMessage.message)
        }

        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        handleNewWebSocket()
        return false
    }

    override fun onPause() {
        cleanup(false)
    }

    override fun onStop() {
        cleanup(true)
    }

    override fun onNeedToSendEvent(event: Int) {
        return
    }

    private fun cleanup(fullCleanup: Boolean){
        stopWebSocket()
        if(fullCleanup){
            scene.signInUIObserver = null
        }
    }

    override fun onBackPressed(): Boolean {
        val currentState = model.state
        return when (currentState) {
            is SignInLayoutState.Start -> {
                if(model.isMultiple) {
                    host.finishScene()
                    false
                } else
                    true
            }
            is SignInLayoutState.LoginValidation -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Start(username, firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                generalDataSource.submitRequest(GeneralRequest.LinkCancel(currentState.username, currentState.domain, model.ephemeralJwt, null))
                false
            }
            is SignInLayoutState.DeniedValidation -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Start(username, firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                generalDataSource.submitRequest(GeneralRequest.LinkCancel(currentState.username, currentState.domain, model.ephemeralJwt, null))
                false
            }
            is SignInLayoutState.InputPassword -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Start(username, firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                false
            }
            is SignInLayoutState.ChangePassword -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Start(username, firstTime = false)
                resetLayout()
                false
            }
            is SignInLayoutState.Connection -> {
                false
            }
            is SignInLayoutState.RemoveDevices -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Start(username, firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                false
            }
        }
    }

    private val onDevicesListItemListener: DevicesListItemListener = object: DevicesListItemListener {
        override fun onDeviceTrashClicked(device: DeviceItem, position: Int): Boolean {
            val checkedDevices = model.devices.filter { it.checked }.size
            scene.showToolbarCount(checkedDevices)
            return true
        }
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) { }

    override fun onOptionsItemSelected(itemId: Int) { }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    interface SignInUIObserver: UIObserver {
        fun onSubmitButtonClicked()
        fun toggleUsernameFocusState(isFocused: Boolean)
        fun onSignUpLabelClicked()
        fun userLoginReady()
        fun onCantAccessDeviceClick()
        fun onRecoveryCodeClicked()
        fun onResendDeviceLinkAuth(username: String, domain: String)
        fun onPasswordChangeListener(newPassword: String)
        fun onRecoveryCodeChangeListener(newPassword: String)
        fun onConfirmPasswordChangeListener(confirmPassword: String)
        fun onUsernameTextChanged(newUsername: String)
        fun onForgotPasswordClick()
        fun onBackPressed()
        fun onXPressed()
        fun onContactSupportPressed()
        fun onProgressHolderFinish()
        fun onRetrySyncOk(result: SignInResult)
        fun onRetrySyncCancel()
        fun onSignInWarningContinue(userName: String, domain: String)
        fun onSetupDevices(devicesListView: VirtualListView)
        fun onTrashPressed(recipient: String, domain: String)
    }

    companion object {
        const val RETRY_TIME_DEFAULT = 5000L
        const val RETRY_TIME_DATA_READY = 10000L
        const val RETRY_TIMES_DEFAULT = 12
        const val RETRY_TIMES_DATA_READY = 18

        //Sync Process Percentages
        const val  SENDING_KEYS_PERCENTAGE = 10
        const val  WAITING_FOR_MAILBOX_PERCENTAGE = 40
        const val  DOWNLOADING_MAILBOX_PERCENTAGE = 70
        const val  SYNC_COMPLETE_PERCENTAGE = 100
    }
}