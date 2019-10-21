package com.criptext.mail.scenes.signup

import android.content.Intent
import com.criptext.mail.*
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.RunnableThrottler
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.signup.data.SignUpRequest
import com.criptext.mail.scenes.signup.data.SignUpResult
import com.criptext.mail.utils.*
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput
import java.util.*

/**
 * Created by sebas on 2/15/18.
 */

class SignUpSceneController(
        private val model: SignUpSceneModel,
        private val scene: SignUpScene,
        private val keyboardManager: KeyboardManager,
        private val host : IHostActivity,
        private val dataSource: BackgroundWorkManager<SignUpRequest, SignUpResult>,
        private val runnableThrottler: RunnableThrottler): SceneController() {

    override val menuResourceId: Int?
        get() = null

    val arePasswordsMatching: Boolean
        get() = model.password == model.confirmPassword

    val isSetRecoveryEmail: Boolean
        get() = model.recoveryEmail.value.isNotEmpty()

    private val isCheckedTermsAndConditions: Boolean
        get() = model.checkTermsAndConditions

    private fun shouldCreateButtonBeEnabled(): Boolean {
        return model.username.value.isNotEmpty()
                && model.username.state !is FormInputState.Error
                &&  model.passwordState is FormInputState.Valid
                && model.recoveryEmail.state !is FormInputState.Error
                && isCheckedTermsAndConditions
    }

    private fun toggleCreateAccountButton() {
        if(shouldCreateButtonBeEnabled()) {
            scene.enableCreateAccountButton()
        } else {
            scene.disableCreateAccountButton()
        }

    }

    private val uiObserver: SignUpUIObserver = object : SignUpUIObserver {
        override fun onUsernameChangedListener(text: String) {
            val newUsername = if (text.isEmpty()) {
                model.username.copy(state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateUsernameOnly(text)
                when (userInput) {
                    is FormData.Valid -> {
                        runnableThrottler.push(Runnable {
                            val newRequest = SignUpRequest.CheckUserAvailability(userInput.value)
                            dataSource.submitRequest(newRequest)
                        })

                        // Only the server can truly validate this
                        model.username.copy(value = userInput.value,
                                            state = FormInputState.Unknown())
                    }

                    is FormData.Error -> {
                        model.username.copy(value = text,
                                            state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.username = newUsername
            scene.setUsernameState(newUsername.state)
            toggleCreateAccountButton()
        }

        override fun onFullNameTextChangeListener(text: String){
            val newFullName = if (text.isEmpty()) {
                model.fullName.copy(state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateFullName(text)
                when (userInput) {
                    is FormData.Valid -> {
                        model.fullName.copy(value = userInput.value,
                                            state = FormInputState.Valid())
                    }

                    is FormData.Error -> {
                        model.fullName.copy(value = text,
                                            state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.fullName = newFullName
            scene.setFullNameState(newFullName.state)
            toggleCreateAccountButton()
        }

        override fun onRecoveryEmailTextChangeListener(text: String) {
            val newRecoveryEmail = if (text.isEmpty()) {
                TextInput(value = text, state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateEmailAddress(text)
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
            model.recoveryEmail = newRecoveryEmail
            scene.setRecoveryEmailState(newRecoveryEmail.state)
            toggleCreateAccountButton()
        }

        override fun onCheckedOptionChanged(state: Boolean) {
            model.checkTermsAndConditions = state
            if (model.checkTermsAndConditions) {
                if (shouldCreateButtonBeEnabled()) {
                    scene.enableCreateAccountButton()
                }
            } else {
                scene.disableCreateAccountButton()
            }
        }

        override fun onTermsAndConditionsClick(){
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("terms"))
        }

        override fun onContactSupportClick() {
            host.launchExternalActivityForResult(ExternalActivityParams.GoToCriptextUrl("contact?version=${BuildConfig.VERSION_NAME}&device=${DeviceUtils.getDeviceName()}&os=${DeviceUtils.getDeviceOS()}"))
        }

        private fun checkPasswords(passwords: Pair<String, String>) {
            if (arePasswordsMatching && passwords.first.length >= minimumPasswordLength) {
                scene.setPasswordError(null)
                scene.togglePasswordSuccess(show = true)
                model.passwordState = FormInputState.Valid()
                if (shouldCreateButtonBeEnabled())
                    scene.enableCreateAccountButton()
            } else if (arePasswordsMatching && passwords.first.isEmpty()) {
                    scene.setPasswordError(null)
                    scene.togglePasswordSuccess(show = false)
                    model.passwordState = FormInputState.Unknown()
                    scene.disableCreateAccountButton()
            } else if (arePasswordsMatching && passwords.first.length < minimumPasswordLength) {
                    scene.togglePasswordSuccess(show = false)
                    val errorMessage = UIMessage(R.string.password_length_error)
                    model.passwordState = FormInputState.Error(errorMessage)
                    scene.setPasswordError(errorMessage)
                    scene.disableCreateAccountButton()
            } else {
                    val errorMessage = UIMessage(R.string.password_mismatch_error)
                    model.passwordState = FormInputState.Error(errorMessage)
                    scene.setPasswordError(errorMessage)
                    scene.togglePasswordSuccess(show = false)
                    scene.disableCreateAccountButton()
            }
        }

        override fun onConfirmPasswordChangedListener(text: String) {
            model.confirmPassword = text
            checkPasswords(Pair(model.confirmPassword, model.password))
        }

        override fun onPasswordChangedListener(text: String) {
            model.password = text
            if(model.confirmPassword.isNotEmpty())
                checkPasswords(Pair(model.password, model.confirmPassword))
        }

        override fun onCreateAccountClick() {
            if(shouldCreateButtonBeEnabled()) {
                if (!isSetRecoveryEmail) {
                    scene.showRecoveryEmailWarningDialog(
                            onRecoveryEmailWarningListener
                    )
                } else {
                    keyboardManager.hideKeyboard()
                    this@SignUpSceneController.submitCreateUser()
                }
            }
        }

        override fun onBackPressed() {
            this@SignUpSceneController.onBackPressed()
        }

        override fun onProgressHolderFinish() {
            if(model.signUpSucceed){
                host.goToScene(MailboxParams(true), false)
            }
        }
    }

    private val dataSourceListener = { result: SignUpResult ->
        when (result) {
            is SignUpResult.RegisterUser -> onUserRegistered(result)
            is SignUpResult.CheckUsernameAvailability -> onCheckedUsernameAvailability(result)
        }
    }

    private fun onCheckedUsernameAvailability(result: SignUpResult.CheckUsernameAvailability) {
        when (result) {
            is SignUpResult.CheckUsernameAvailability.Success -> {
                if (result.isAvailable) {
                    scene.setUsernameState(FormInputState.Valid())
                    toggleCreateAccountButton()
                } else {
                    val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
                    model.username = model.username.copy(state = newState)
                    scene.setUsernameState(newState)
                }
            }
            is SignUpResult.CheckUsernameAvailability.Failure -> {
                val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
                model.username = model.username.copy(state = newState)
                scene.setUsernameState(newState)
            }
        }
    }

    private fun handleRegisterUserFailure(result: SignUpResult.RegisterUser.Failure) {
        scene.initListeners(uiObserver)
        scene.showError(result.message)
        resetWidgetsFromModel()
        when(result.exception)
        {
            is ServerErrorException -> {
                when(result.exception.errorCode){
                    ServerCodes.BadRequest -> {
                        val newState = FormInputState.Error(result.message)
                        model.username = model.username.copy(state = newState)

                        scene.setUsernameState(newState)
                        scene.disableCreateAccountButton()
                    }
                    ServerCodes.TooManyRequests -> {
                        scene.showError(result.message)
                        host.exitToScene(
                                params = SignInParams(),
                                activityMessage = null,
                                forceAnimation = false,
                                deletePastIntents = true)
                    }
                }
            }
        }
    }
    private fun onUserRegistered(result: SignUpResult.RegisterUser) {
        when (result) {
            is SignUpResult.RegisterUser.Success -> {
                model.signUpSucceed = true
            }
            is SignUpResult.RegisterUser.Failure -> handleRegisterUserFailure(result)
        }
    }

    private fun submitCreateUser() {
        scene.showKeyGenerationHolder()

        val hashedPassword = model.password.sha256()
        val newAccount = IncompleteAccount(
                username = model.username.value,
                name = model.fullName.value,
                password = hashedPassword,
                deviceId = 1,
                recoveryEmail = if (isSetRecoveryEmail) model.recoveryEmail.value else null
        )

        val req = SignUpRequest.RegisterUser(
                account = newAccount,
                recipientId = model.username.value,
                isMultiple = model.isMultiple
        )
        dataSource.submitRequest(req)
    }

    private fun resetWidgetsFromModel() {
        scene.resetSceneWidgetsFromModel(
                username = model.username,
                recoveryEmail = model.recoveryEmail,
                password = model.password,
                confirmPassword = model.confirmPassword,
                fullName = model.fullName,
                isChecked = model.checkTermsAndConditions
        )
    }

    val onRecoveryEmailWarningListener = object : OnRecoveryEmailWarningListener {
        override fun willAssignRecoverEmail() {
        }

        override fun denyWillAssignRecoverEmail() {
            this@SignUpSceneController.submitCreateUser()
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        scene.showFormHolder()
        if(model.username.value.isNotEmpty()) {
            scene.resetSceneWidgetsFromModel(model.username, model.fullName, model.password,
                    model.confirmPassword, model.recoveryEmail, model.checkTermsAndConditions)
            uiObserver.onUsernameChangedListener(model.username.value)
            uiObserver.onFullNameTextChangeListener(model.fullName.value)
            uiObserver.onPasswordChangedListener(model.password)
            uiObserver.onConfirmPasswordChangedListener(model.confirmPassword)
            uiObserver.onCheckedOptionChanged(model.checkTermsAndConditions)
            toggleCreateAccountButton()
        }
        scene.initListeners(
                uiObserver = uiObserver
        )
        scene.disableCreateAccountButton()
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    override fun onPause() {

    }

    override fun onStop() {
        dataSource.listener = null
        scene.uiObserver = null
    }

    override fun onBackPressed(): Boolean {
        host.exitToScene(
                params = SignInParams(),
                activityMessage = null,
                forceAnimation = false)
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    interface SignUpUIObserver {
        fun onCreateAccountClick()
        fun onPasswordChangedListener(text: String)
        fun onConfirmPasswordChangedListener(text: String)
        fun onUsernameChangedListener(text: String)
        fun onFullNameTextChangeListener(text: String)
        fun onRecoveryEmailTextChangeListener(text: String)
        fun onCheckedOptionChanged(state: Boolean)
        fun onTermsAndConditionsClick()
        fun onContactSupportClick()
        fun onBackPressed()
        fun onProgressHolderFinish()
    }

    companion object {
        val minimumPasswordLength = 8
    }
}
