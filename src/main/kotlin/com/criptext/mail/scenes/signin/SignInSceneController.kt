package com.criptext.mail.scenes.signin

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignUpParams
import com.criptext.mail.scenes.signin.data.CreateSessionWorker
import com.criptext.mail.scenes.signin.data.SignInDataSource
import com.criptext.mail.scenes.signin.data.SignInRequest
import com.criptext.mail.scenes.signin.data.SignInResult
import com.criptext.mail.scenes.signin.holders.SignInLayoutState
import com.criptext.mail.temporalwebsocket.TempWebSocketClient
import com.criptext.mail.temporalwebsocket.TempWebSocketController
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.sha256
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.ProgressButtonState
import com.criptext.mail.websocket.NVWebSocketClient
import com.criptext.mail.websocket.WebSocketEventListener

/**
 * Created by sebas on 2/15/18.
 */

class SignInSceneController(
        private val model: SignInSceneModel,
        private val scene: SignInScene,
        private val host: IHostActivity,
        private val generalDataSource: GeneralDataSource,
        private val dataSource: SignInDataSource,
        private val keyboard: KeyboardManager): SceneController() {

    override val menuResourceId: Int? = null

    private var tempWebSocket: TempWebSocketController? = null

    private val dataSourceListener = { result: SignInResult ->
        when (result) {
            is SignInResult.AuthenticateUser -> onUserAuthenticated(result)
            is SignInResult.CheckUsernameAvailability -> onCheckUsernameAvailability(result)
            is SignInResult.LinkBegin -> onLinkBegin(result)
            is SignInResult.LinkAuth -> onLinkAuth(result)
            is SignInResult.CreateSessionFromLink -> onCreateSessionFromLink(result)
        }
    }

    private val generalDataSourceListener = { result: GeneralResult ->
        when (result) {
            is GeneralResult.ResetPassword -> onForgotPassword(result)
        }
    }

    private fun onAuthenticationFailed(result: SignInResult.AuthenticateUser.Failure) {
        scene.showError(result.message)

        val currentState = model.state
        if (currentState is SignInLayoutState.InputPassword) {
            model.state = currentState.copy(password = "",
                    buttonState = ProgressButtonState.disabled)
            scene.resetInput()
        }
    }

    private fun onForgotPassword(result: GeneralResult.ResetPassword){
        scene.toggleForgotPasswordClickable(true)
        when(result){
            is GeneralResult.ResetPassword.Success -> {
                scene.showResetPasswordDialog(result.email)
            }
            is GeneralResult.ResetPassword.Failure -> scene.showError(result.message)
        }
    }

    private fun onCheckUsernameAvailability(result: SignInResult.CheckUsernameAvailability) {
        when (result) {
            is SignInResult.CheckUsernameAvailability.Success -> {
                if(result.userExists) {
                    keyboard.hideKeyboard()
                    //LINK DEVICE FEATURE
                    model.state = SignInLayoutState.LoginValidation(username = result.username)
                    dataSource.submitRequest(SignInRequest.LinkBegin(result.username))
                }
                else{
                    scene.drawInputError(UIMessage(R.string.username_doesnt_exist))
                }
            }
            is SignInResult.CheckUsernameAvailability.Failure -> scene.showError(UIMessage(R.string.error_checking_availability))
        }
        scene.setSubmitButtonState(ProgressButtonState.enabled)
    }

    private fun onLinkBegin(result: SignInResult.LinkBegin) {
        when (result) {
            is SignInResult.LinkBegin.Success -> {
                scene.initLayout(model.state, uiObserver)
                val currentState = model.state as SignInLayoutState.LoginValidation
                model.ephemeralJwt = result.ephemeralJwt
                handleNewTemporalWebSocket()
                dataSource.submitRequest(SignInRequest.LinkAuth(currentState.username, model.ephemeralJwt))
            }
            is SignInResult.LinkBegin.Failure -> {
                val currentState = model.state as SignInLayoutState.LoginValidation
                onAcceptPasswordLogin(currentState.username)
            }
        }
    }

    private fun onLinkAuth(result: SignInResult.LinkAuth) {
        scene.toggleResendClickable(true)
        when (result) {
            is SignInResult.LinkAuth.Success -> {

            }
            is SignInResult.LinkAuth.Failure -> {
                val currentState = model.state as SignInLayoutState.LoginValidation
                scene.showError(UIMessage(R.string.server_error_exception))
            }
        }
    }

    private fun onCreateSessionFromLink(result: SignInResult.CreateSessionFromLink) {
        when (result) {
            is SignInResult.CreateSessionFromLink.Success -> {
                //This is for the second part of link devices
//                model.state = SignInLayoutState.WaitForApproval()
//                scene.initLayout(model.state, uiObserver)
                scene.showKeyGenerationHolder()
            }
            is SignInResult.CreateSessionFromLink.Failure -> {
                val authResult =
                        SignInResult.AuthenticateUser.Failure(result.message, Exception())
                onAuthenticationFailed(authResult)
            }
        }
    }

    private fun onUserAuthenticated(result: SignInResult.AuthenticateUser) {
        when (result) {
            is SignInResult.AuthenticateUser.Success -> {
                scene.showKeyGenerationHolder()
            }
            is SignInResult.AuthenticateUser.Failure -> onAuthenticationFailed(result)
        }
    }

    private fun onAcceptPasswordLogin(username: String){
        model.state = SignInLayoutState.InputPassword(
                username = username,
                password = "",
                buttonState = ProgressButtonState.disabled)
        scene.initLayout(model.state, uiObserver)
    }

    private val passwordLoginDialogListener = object : OnPasswordLoginDialogListener {

        override fun acceptPasswordLogin(username: String) {
            onAcceptPasswordLogin(username)
        }

        override fun cancelPasswordLogin() {
        }
    }

    private fun handleNewTemporalWebSocket(){
        tempWebSocket = TempWebSocketController(TempWebSocketClient(), model.ephemeralJwt)
        tempWebSocket?.setListener(webSocketEventListener)
    }

    private fun onSignInButtonClicked(currentState: SignInLayoutState.Start) {
        val userInput = AccountDataValidator.validateUsername(currentState.username)
        when (userInput) {
            is FormData.Valid -> {
                val newRequest = SignInRequest.CheckUserAvailability(userInput.value)
                dataSource.submitRequest(newRequest)
                scene.setSubmitButtonState(ProgressButtonState.waiting)
            }
            is FormData.Error ->
                scene.drawInputError(userInput.message)
        }
    }

    private fun onSignInButtonClicked(currentState: SignInLayoutState.InputPassword) {
        if (currentState.password.isNotEmpty()) {
            val newButtonState = ProgressButtonState.waiting
            model.state = currentState.copy(buttonState = newButtonState)
            scene.setSubmitButtonState(newButtonState)

            val hashedPassword = currentState.password.sha256()
            val req = SignInRequest.AuthenticateUser(
                    username = currentState.username,
                    password = hashedPassword
            )
            dataSource.submitRequest(req)
        }
    }

    private val webSocketEventListener = object : WebSocketEventListener {
        override fun onDeviceLinkAuthDeny() {
            host.runOnUiThread(Runnable {
                scene.showLinkBeginError()
            })
        }

        override fun onDeviceLinkAuthAccept(deviceId: Int, name: String) {
            val currentState = model.state as SignInLayoutState.LoginValidation

            dataSource.submitRequest(SignInRequest.CreateSessionFromLink(name = name,
                    username = currentState.username,
                    randomId = deviceId, ephemeralJwt = model.ephemeralJwt))
        }

        override fun onKeyBundleUploaded(deviceId: Int) {

        }

        override fun onDeviceLinkAuthRequest(untrustedDeviceInfo: UntrustedDeviceInfo) {

        }

        override fun onNewEvent() {

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
        override fun onResendDeviceLinkAuth(username: String) {
            dataSource.submitRequest(SignInRequest.LinkAuth(username, model.ephemeralJwt))
        }

        override fun onProgressHolderFinish() {
            host.goToScene(MailboxParams(), false)
        }

        override fun onBackPressed() {
            this@SignInSceneController.onBackPressed()
        }

        override fun onSubmitButtonClicked() {
            val state = model.state
            when (state) {
                is SignInLayoutState.Start -> onSignInButtonClicked(state)
                is SignInLayoutState.InputPassword -> onSignInButtonClicked(state)
            }
        }

        override fun onForgotPasswordClick() {
            scene.toggleForgotPasswordClickable(false)
            val currentState = model.state as SignInLayoutState.InputPassword
            generalDataSource.submitRequest(GeneralRequest.ResetPassword(currentState.username))
        }

        override fun onCantAccessDeviceClick(){
            scene.showPasswordLoginDialog(
                    onPasswordLoginDialogListener = this@SignInSceneController.passwordLoginDialogListener)
        }
        override fun userLoginReady() {
            host.goToScene(MailboxParams(), false)
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
            }
        }
        override fun onUsernameTextChanged(newUsername: String) {
            model.state = SignInLayoutState.Start(username = newUsername, firstTime = false)
            val buttonState = if (newUsername.isNotEmpty()) ProgressButtonState.enabled
                              else ProgressButtonState.disabled
            scene.setSubmitButtonState(buttonState)
        }

        override fun onSignUpLabelClicked() {
            host.goToScene(SignUpParams(), false)
        }
    }

    private fun resetLayout() {
        scene.initLayout(model.state, uiObserver)
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        scene.initLayout(state = model.state, signInUIObserver = uiObserver)
        if(activityMessage != null && activityMessage is ActivityMessage.ShowUIMessage){
            scene.showError(activityMessage.message)
        }

        return false
    }

    override fun onStop() {
        scene.signInUIObserver = null
        if(tempWebSocket != null) {
            tempWebSocket?.clearListener(webSocketEventListener)
            tempWebSocket?.disconnect()
        }
    }

    override fun onBackPressed(): Boolean {
        val currentState = model.state
        return when (currentState) {
            is SignInLayoutState.Start -> true
            is SignInLayoutState.LoginValidation -> {
                model.state = SignInLayoutState.Start(currentState.username, firstTime = false)
                resetLayout()
                false
            }
            is SignInLayoutState.InputPassword -> {
                model.state = SignInLayoutState.Start(currentState.username, firstTime = false)
                resetLayout()
                false
            }
            is SignInLayoutState.WaitForApproval -> {
                model.state = SignInLayoutState.Start("", firstTime = false)
                resetLayout()
                false
            }
        }
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    interface SignInUIObserver {
        fun onSubmitButtonClicked()
        fun toggleUsernameFocusState(isFocused: Boolean)
        fun onSignUpLabelClicked()
        fun userLoginReady()
        fun onCantAccessDeviceClick()
        fun onResendDeviceLinkAuth(username: String)
        fun onPasswordChangeListener(newPassword: String)
        fun onUsernameTextChanged(newUsername: String)
        fun onForgotPasswordClick()
        fun onBackPressed()
        fun onProgressHolderFinish()
    }
}