package com.criptext.mail.scenes.signin

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ContactDomainCheckData
import com.criptext.mail.scenes.params.ImportMailboxParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignUpParams
import com.criptext.mail.scenes.params.WebViewParams
import com.criptext.mail.scenes.settings.DevicesListItemListener
import com.criptext.mail.scenes.settings.changepassword.ChangePasswordController
import com.criptext.mail.scenes.settings.devices.data.DeviceItem
import com.criptext.mail.scenes.signin.data.*
import com.criptext.mail.scenes.signin.holders.SignInLayoutState
import com.criptext.mail.scenes.signin.workers.AuthenticateUserWorker
import com.criptext.mail.scenes.signin.workers.RecoveryCodeWorker
import com.criptext.mail.utils.*
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.DialogType
import com.criptext.mail.utils.ui.data.ActivityTransitionAnimationData
import com.criptext.mail.utils.uiobserver.UIObserver
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.ProgressButtonState
import com.criptext.mail.websocket.CriptextWebSocketFactory
import com.criptext.mail.websocket.WebSocketEventListener
import com.criptext.mail.websocket.WebSocketEventPublisher
import java.util.*

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
        private val keyboard: KeyboardManager): SceneController(host, null, storage) {

    override val menuResourceId: Int? = R.menu.menu_remove_device_holder

    private var webSocket: WebSocketEventPublisher? = null

    private val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    private val dataSourceListener = { result: SignInResult ->
        when (result) {
            is SignInResult.AuthenticateUser -> onUserAuthenticated(result)
            is SignInResult.FindDevices -> onFindDevices(result)
            is SignInResult.RemoveDevices -> onRemoveDevices(result)
            is SignInResult.RecoveryCode -> onGenerateRecoveryCode(result)
            is SignInResult.GetMaxDevices -> onGetMaxDevices(result)
        }
    }

    private val generalDataSourceListener = { result: GeneralResult ->
        when (result) {
            is GeneralResult.ResetPassword -> onForgotPassword(result)
            is GeneralResult.DeviceRemoved -> onDeviceRemoved(result)
        }
    }

    private var deviceWrapperListController: DeviceWrapperListController? = null

    private fun onAuthenticationFailed(errorMessage: UIMessage) {
        scene.showError(errorMessage)

        val currentState = model.state
        if (currentState is SignInLayoutState.Login) {
            model.state = currentState.copy(password = "",
                    buttonState = ProgressButtonState.disabled)
            scene.resetInput()
        } else {
            model.state = SignInLayoutState.Login(
                    username = "",
                    recipientId = "",
                    password = "",
                    buttonState = ProgressButtonState.disabled,
                    domain = "",
                    firstTime = false)
            scene.initLayout(model, uiObserver)
        }
    }

    private fun onForgotPassword(result: GeneralResult.ResetPassword){
        if(model.state is SignInLayoutState.ForgotPassword) {
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
                model.state = SignInLayoutState.Start(false)
                resetLayout()
            }
        }
    }

    private fun onFindDevices(result: SignInResult.FindDevices){
        when (result) {
            is SignInResult.FindDevices.Success -> {
                model.devices = result.devices
                when(model.state){
                    is SignInLayoutState.LoginValidation -> {
                        val currentState = model.state as SignInLayoutState.LoginValidation
                        model.state = SignInLayoutState.RemoveDevices(
                                username = currentState.username,
                                domain = currentState.domain,
                                password = currentState.password,
                                buttonState = ProgressButtonState.disabled,
                                devices = model.devices
                        )
                        scene.initLayout(model, uiObserver, onDevicesListItemListener)
                    }
                    else -> {
                        val currentState = model.state as SignInLayoutState.Login
                        model.state = SignInLayoutState.RemoveDevices(
                                username = currentState.username,
                                domain = currentState.domain,
                                password = currentState.password,
                                buttonState = ProgressButtonState.disabled,
                                devices = model.devices
                        )
                        scene.initLayout(model, uiObserver)
                    }
                }
            }
            is SignInResult.FindDevices.Failure -> {
                onAuthenticationFailed(result.message)
            }
        }
    }

    private fun onGetMaxDevices(result: SignInResult.GetMaxDevices){
        when (result) {
            is SignInResult.GetMaxDevices.Success -> {
                model.maxDevices = result.maxDevices
                if(model.devices.size < model.maxDevices) {
                    val state = model.state as SignInLayoutState.RemoveDevices

                    model.realSecurePassword = state.password.sha256()
                    scene.setSubmitButtonState(ProgressButtonState.enabled)
                }
            }
        }
    }

    private fun onRemoveDevices(result: SignInResult.RemoveDevices){
        when (result) {
            is SignInResult.RemoveDevices.Success -> {
                deviceWrapperListController?.remove(result.deviceIds)
                val state = model.state as SignInLayoutState.RemoveDevices
                model.realSecurePassword = state.password.sha256()
                model.ephemeralJwt = result.newToken
                scene.setSubmitButtonState(ProgressButtonState.enabled)
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
                    host.goToScene(ImportMailboxParams(), false)
                } else {
                    val currentState = model.state as SignInLayoutState.Login
                    model.state = SignInLayoutState.LoginValidation(
                            username = currentState.username,
                            domain = currentState.domain,
                            password = currentState.password,
                            recoveryCode = "",
                            needToRemoveDevices = model.needToRemoveDevices,
                            recoveryAddress = result.emailAddress
                    )
                    scene.initLayout(model, uiObserver)
                }
            }
            is SignInResult.RecoveryCode.Failure -> {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
                if(result.isValidate){
                    if(result.exception is RecoveryCodeWorker.NeedToRemoveDevices){
                        model.ephemeralJwt = result.exception.tempToken
                        dataSource.submitRequest(SignInRequest.FindDevices(model.ephemeralJwt))
                    } else {
                        scene.drawInputError(result.message)
                    }
                } else {
                    scene.showError(result.message)
                }
            }
        }
    }

    private fun onUserAuthenticated(result: SignInResult.AuthenticateUser) {
        when (result) {
            is SignInResult.AuthenticateUser.Success -> {
                host.goToScene(ImportMailboxParams(), false)
            }
            is SignInResult.AuthenticateUser.Failure -> {
                when(result.exception){
                    is ServerErrorException -> {
                        if(result.exception.errorCode == ServerCodes.PreconditionFail){
                            val currentState = model.state as SignInLayoutState.Login
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
                    is AuthenticateUserWorker.LoginNeededAction -> {
                        model.hasTwoFA = result.exception.hastwoFA
                        model.needToRemoveDevices = result.exception.hasTooManyDevices
                        model.ephemeralJwt = result.exception.ephemeralJwt
                        val currentState = model.state as SignInLayoutState.Login
                        if(model.hasTwoFA){
                            dataSource.submitRequest(SignInRequest.RecoveryCode(
                                    recipientId = currentState.recipientId,
                                    domain = currentState.domain,
                                    tempToken = model.ephemeralJwt,
                                    isMultiple = model.isMultiple,
                                    needToRemoveDevices = model.needToRemoveDevices
                            ))
                        } else if(model.needToRemoveDevices){
                            dataSource.submitRequest(SignInRequest.FindDevices(
                                    temporalJwt = model.ephemeralJwt
                            ))
                        }
                    }
                    else -> {
                        onAuthenticationFailed(result.message)
                    }
                }
            }
        }
    }

    private fun handleNewWebSocket(){
        if(model.activeAccount != null) {
            webSocket = webSocketFactory.createWebSocket(model.activeAccount!!.jwt)
            webSocket?.setListener(webSocketEventListener)
        }
    }

    private fun onSignInButtonClicked(currentState: SignInLayoutState) {
        keyboard.hideKeyboard()
        when(currentState){
            is SignInLayoutState.Login -> {
                val userInput = AccountDataValidator.validateUsername(currentState.username)
                when (userInput) {
                    is FormData.Valid -> {
                        val (recipientId, domain) = if (AccountDataValidator.validateEmailAddress(userInput.value) is FormData.Valid) {
                            val nonCriptextDomain = EmailAddressUtils.extractEmailAddressDomain(userInput.value)
                            Pair(EmailAddressUtils.extractRecipientIdFromAddress(userInput.value, nonCriptextDomain),
                                    nonCriptextDomain
                            )
                        } else {
                            Pair(userInput.value, Contact.mainDomain)
                        }

                        if (model.checkedDomains.map { it.name }.contains(domain))
                            scene.drawInputError(UIMessage(R.string.username_is_not_criptext))
                        else if (currentState.password.isNotEmpty()) {
                            val newButtonState = ProgressButtonState.waiting
                            val hashedPassword = currentState.password.sha256()
                            model.state = currentState.copy(recipientId = recipientId,
                                    domain = domain, buttonState = newButtonState)
                            scene.setSubmitButtonState(newButtonState)
                            val state = model.state as SignInLayoutState.Login


                            val userData = UserData(state.recipientId, state.domain, hashedPassword, null)
                            val req = SignInRequest.AuthenticateUser(
                                    userData = userData,
                                    isMultiple = model.isMultiple
                            )


                            val oldAccounts = AccountUtils.getLastLoggedAccounts(storage)
                            if (oldAccounts.isNotEmpty() && state.recipientId.plus("@${state.domain}") !in oldAccounts)
                                scene.showSignInWarningDialog(
                                        oldAccountName = oldAccounts.joinToString {
                                            if (AccountDataValidator.validateEmailAddress(it) is FormData.Valid) it
                                            else it.plus(EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX)
                                        },
                                        newUserData = userData
                                )
                            else {
                                val lastLoggedAccounts = AccountUtils.getLastLoggedAccounts(storage)
                                if (!lastLoggedAccounts.contains(state.username))
                                    model.showRestoreBackupDialog = true

                                dataSource.submitRequest(req)
                            }
                        }
                    }
                    is FormData.Error ->
                        scene.drawInputError(userInput.message)
                }
            }
            is SignInLayoutState.ChangePassword -> {
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
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onLinkDeviceDismiss(accountEmail: String) {

        }

        override fun onSyncDeviceDismiss(accountEmail: String) {

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

        }

        override fun onDeviceLinkAuthDeny() {

        }

        override fun onDeviceLinkAuthAccept(linkStatusData: LinkStatusData) {

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

    private val uiObserver = object : SignInUIObserver(generalDataSource, host) {
        override fun onRecoveryCodeChangeListener(newCode: String) {
            val currentState = model.state as SignInLayoutState.LoginValidation
            val buttonState = when(val input = AccountDataValidator.validateRecoveryCode(newCode)){
                is FormData.Valid -> {
                    model.state = currentState.copy(recoveryCode = newCode)
                    scene.drawInputError(null)
                    ProgressButtonState.enabled
                }
                is FormData.Error -> {
                    scene.drawInputError(input.message)
                    ProgressButtonState.disabled
                }
            }
            scene.setSubmitButtonState(buttonState)
        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {

        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            when(result){
                is DialogResult.DialogCriptextPlus -> {
                    if(result.type is DialogType.CriptextPlus){
                        host.goToScene(
                                params = WebViewParams(
                                        url = Hosts.billing(model.ephemeralJwt, Locale.getDefault().language),
                                        title = null
                                ),
                                activityMessage = null,
                                keep = true,
                                animationData = ActivityTransitionAnimationData(
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
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCancelButtonPressed() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSnackbarClicked() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSetupDevices(devicesListView: VirtualListView) {
            deviceWrapperListController = DeviceWrapperListController(model, devicesListView)
        }

        override fun onSignInWarningContinue(newUserData: UserData) {
            val req = SignInRequest.AuthenticateUser(
                    userData = newUserData,
                    isMultiple = model.isMultiple
            )
            val lastLoggedAccounts = AccountUtils.getLastLoggedAccounts(storage)
            if(!lastLoggedAccounts.contains(newUserData.username))
                model.showRestoreBackupDialog = true

            dataSource.submitRequest(req)
        }

        override fun onBackPressed() {
            model.linkDeviceState = LinkDeviceState.Begin()
            this@SignInSceneController.onBackPressed()
        }

        override fun onContactSupportPressed() {
            host.goToScene(
                    params = WebViewParams(
                            url = Hosts.HELP_DESK_URL,
                            title = null
                    ),
                    activityMessage = null,
                    keep = true,
                    animationData = ActivityTransitionAnimationData(
                            forceAnimation = true,
                            enterAnim = R.anim.slide_in_up,
                            exitAnim = R.anim.stay
                    )
            )
        }

        override fun onSubmitButtonClicked() {
            val state = model.state
            when (state) {
                is SignInLayoutState.Start -> {
                    model.state = SignInLayoutState.Login(
                            username = "",
                            password = "",
                            buttonState = ProgressButtonState.disabled,
                            domain = "",
                            firstTime = state.firstTime,
                            recipientId = "")
                    scene.initLayout(model, this)
                }
                is SignInLayoutState.Login -> onSignInButtonClicked(state)
                is SignInLayoutState.ChangePassword -> {
                    keyboard.hideKeyboard()
                    onSignInButtonClicked(state)
                }
                is SignInLayoutState.ForgotPassword -> {
                    scene.toggleForgotPasswordClickable(false)
                    scene.setSubmitButtonState(ProgressButtonState.waiting)
                    val userInput = AccountDataValidator.validateUsername(state.username)
                    when (userInput) {
                        is FormData.Valid -> {
                            val (recipientId, domain) = if (AccountDataValidator.validateEmailAddress(userInput.value) is FormData.Valid) {
                                val nonCriptextDomain = EmailAddressUtils.extractEmailAddressDomain(userInput.value)
                                Pair(EmailAddressUtils.extractRecipientIdFromAddress(userInput.value, nonCriptextDomain),
                                        nonCriptextDomain
                                )
                            } else {
                                Pair(userInput.value, Contact.mainDomain)
                            }
                            generalDataSource.submitRequest(GeneralRequest.ResetPassword(recipientId,
                                    domain))
                        }
                        is FormData.Error -> {
                            scene.setSubmitButtonState(ProgressButtonState.disabled)
                            scene.drawInputError(userInput.message)
                        }
                    }
                }
                is SignInLayoutState.LoginValidation -> {
                    scene.setSubmitButtonState(ProgressButtonState.waiting)
                    val currentState = model.state as SignInLayoutState.LoginValidation
                    dataSource.submitRequest(SignInRequest.RecoveryCode(currentState.username,
                            currentState.domain, model.ephemeralJwt, model.isMultiple,
                            model.needToRemoveDevices, currentState.recoveryCode))
                }
                is SignInLayoutState.RemoveDevices -> {
                    scene.setSubmitButtonState(ProgressButtonState.waiting)
                    val currentState = model.state as SignInLayoutState.RemoveDevices
                    dataSource.submitRequest(SignInRequest.AuthenticateUser(
                            userData = UserData(
                                    username = currentState.username,
                                    domain = currentState.domain,
                                    password = currentState.password.sha256(),
                                    oldPassword = null
                            ),
                            isMultiple = model.isMultiple,
                            tempToken = model.ephemeralJwt
                    ))
                }
            }
        }

        override fun onForgotPasswordClick() {
            val currentState = model.state as SignInLayoutState.Login
            model.state = SignInLayoutState.ForgotPassword(currentState.username)
            scene.initLayout(model, this)
        }

        override fun userLoginReady() {
            host.goToScene(MailboxParams(), false, true, ActivityMessage.RefreshUI())
        }

        override fun onResendRecoveryCode() {
            val currentState = model.state as SignInLayoutState.LoginValidation
            dataSource.submitRequest(SignInRequest.RecoveryCode(currentState.username,
                    currentState.domain, model.ephemeralJwt, model.isMultiple,
                    model.needToRemoveDevices, currentState.recoveryCode))
        }

        override fun toggleUsernameFocusState(isFocused: Boolean) {
        }

        override fun onPasswordChangeListener(newPassword: String) {
            val currentState = model.state
            if (currentState is SignInLayoutState.Login) {
                val buttonState = if (currentState.username.isNotEmpty() && newPassword.isNotEmpty()
                        && newPassword.length >= AccountDataValidator.minimumPasswordLength)
                    ProgressButtonState.enabled
                else
                    ProgressButtonState.disabled
                model.state = currentState.copy(
                        username = currentState.username,
                        password = newPassword,
                        buttonState = buttonState)
                scene.setSubmitButtonState(state = buttonState)
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
            val currentState = model.state
            when(currentState){
                is SignInLayoutState.Login -> {
                    val buttonState = if (newUsername.isNotEmpty() && currentState.password.isNotEmpty()
                            && currentState.password.length >= AccountDataValidator.minimumPasswordLength)
                        ProgressButtonState.enabled
                    else
                        ProgressButtonState.disabled
                    model.state = currentState.copy(username = newUsername, password = currentState.password, firstTime = false)
                    scene.setSubmitButtonState(buttonState)
                }
                is SignInLayoutState.ForgotPassword -> {
                    val buttonState = if (newUsername.isNotEmpty()
                            && AccountDataValidator.validateEmailAddress(newUsername) is FormData.Valid)
                        ProgressButtonState.enabled
                    else ProgressButtonState.disabled
                    model.state = currentState.copy(username = newUsername)
                    scene.setSubmitButtonState(buttonState)
                }
            }
        }

        override fun onSignUpLabelClicked() {
            host.goToScene(SignUpParams(model.isMultiple), true)
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
        if(model.state is SignInLayoutState.RemoveDevices)
            dataSource.submitRequest(SignInRequest.GetMaxDevices(model.ephemeralJwt))
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
            is SignInLayoutState.Login -> {
                model.state = SignInLayoutState.Start(currentState.firstTime)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                false
            }
            is SignInLayoutState.LoginValidation -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Login(
                        username = username,
                        recipientId = currentState.username,
                        password = currentState.password,
                        buttonState = ProgressButtonState.enabled,
                        domain = currentState.domain,
                        firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                model.ephemeralJwt = ""
                resetLayout()
                false
            }
            is SignInLayoutState.ForgotPassword -> {
                model.state = SignInLayoutState.Login(
                        username = "",
                        recipientId = "",
                        password = "",
                        buttonState = ProgressButtonState.disabled,
                        domain = "",
                        firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                false
            }
            is SignInLayoutState.DeniedValidation -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                //model.state = SignInLayoutState.Login(username, firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                generalDataSource.submitRequest(GeneralRequest.LinkCancel(currentState.username, currentState.domain, model.ephemeralJwt, null))
                false
            }
            is SignInLayoutState.ChangePassword -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Login(
                        username = username,
                        recipientId = currentState.username,
                        domain = currentState.domain,
                        password = currentState.oldPassword,
                        buttonState = ProgressButtonState.enabled,
                        firstTime = false)
                resetLayout()
                false
            }
            is SignInLayoutState.RemoveDevices -> {
                val username = if(currentState.domain != Contact.mainDomain)
                    currentState.username.plus("@${currentState.domain}")
                else currentState.username
                model.state = SignInLayoutState.Login(
                        username = username,
                        recipientId = currentState.username,
                        domain = currentState.domain,
                        password = currentState.password,
                        buttonState = ProgressButtonState.enabled,
                        firstTime = false)
                model.needToRemoveDevices = false
                model.realSecurePassword = null
                resetLayout()
                host.dismissCriptextPlusDialog()
                false
            }
        }
    }

    private val onDevicesListItemListener: DevicesListItemListener = object: DevicesListItemListener {
        override fun onDeviceCheckChanged(): Boolean {
            return true
        }

        override fun onDeviceTrashClicked(device: DeviceItem, position: Int): Boolean {
            val currentState = model.state as SignInLayoutState.RemoveDevices
            dataSource.submitRequest(SignInRequest.RemoveDevices(
                    userData = UserData(currentState.username, currentState.domain, currentState.password, null),
                    tempToken = model.ephemeralJwt,
                    deviceIds = listOf(device.id),
                    deviceIndexes = listOf(position)
            ))
            return true
        }
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) { }

    override fun onOptionsItemSelected(itemId: Int) { }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    abstract class SignInUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
        abstract fun onSubmitButtonClicked()
        abstract fun toggleUsernameFocusState(isFocused: Boolean)
        abstract fun onSignUpLabelClicked()
        abstract fun userLoginReady()
        abstract fun onResendRecoveryCode()
        abstract fun onPasswordChangeListener(newPassword: String)
        abstract fun onRecoveryCodeChangeListener(newCode: String)
        abstract fun onConfirmPasswordChangeListener(confirmPassword: String)
        abstract fun onUsernameTextChanged(newUsername: String)
        abstract fun onForgotPasswordClick()
        abstract fun onBackPressed()
        abstract fun onContactSupportPressed()
        abstract fun onSignInWarningContinue(newUserData: UserData)
        abstract fun onSetupDevices(devicesListView: VirtualListView)
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