package com.email.scenes.signup

import com.email.IHostActivity
import com.email.R
import com.email.api.ServerErrorException
import com.email.bgworker.RunnableThrottler
import com.email.bgworker.WorkHandler
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.params.MailboxParams
import com.email.scenes.params.SignInParams
import com.email.scenes.signup.data.FormValidator
import com.email.scenes.signup.data.SignUpRequest
import com.email.scenes.signup.data.SignUpResult
import com.email.utils.form.FormData
import com.email.utils.form.FormInputState
import com.email.utils.UIMessage
import com.email.utils.form.TextInput

/**
 * Created by sebas on 2/15/18.
 */

class SignUpSceneController(
        private val model: SignUpSceneModel,
        private val scene: SignUpScene,
        private val host : IHostActivity,
        private val dataSource: WorkHandler<SignUpRequest, SignUpResult>,
        private val runnableThrottler: RunnableThrottler): SceneController() {

    private val validator = FormValidator()

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
                val userInput = validator.validateUsername(text)
                when (userInput) {
                    is FormData.Valid -> {
                        runnableThrottler.push(Runnable {
                            val newRequest = SignUpRequest.CheckUserAvailabilty(userInput.value)
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
                val userInput = validator.validateFullName(text)
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
                val userInput = validator.validateRecoveryEmailAddress(text)
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
            TODO("READ TERMS AND CONDITIONS.")
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
            checkPasswords(Pair(model.password, model.confirmPassword))
        }

        override fun onCreateAccountClick() {
            if(shouldCreateButtonBeEnabled()) {
                if (!isSetRecoveryEmail) {
                    scene.showRecoveryEmailWarningDialog(
                            onRecoveryEmailWarningListener
                    )
                } else {
                    this@SignUpSceneController.submitCreateUser()
                }
            }
        }

        override fun onRegisterUserSuccess(){
            host.goToScene(MailboxParams(), false)
        }

        override fun onBackPressed() {
            this@SignUpSceneController.onBackPressed()
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
                if (result.isAvailable)
                    scene.setUsernameState(FormInputState.Valid())
                else {
                    val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
                    model.username = model.username.copy(state = newState)
                    scene.setUsernameState(newState)
                }
            }
        }
    }

    private fun handleRegisterUserFailure(result: SignUpResult.RegisterUser.Failure) {
        scene.showError(result.message)
        resetWidgetsFromModel()
        if(result.exception is ServerErrorException &&
            result.exception.errorCode == 400) {
            val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
            model.username = model.username.copy(state = newState)

            scene.setUsernameState(newState)
            scene.disableCreateAccountButton()
        }
    }
    private fun onUserRegistered(result: SignUpResult.RegisterUser) {
        when (result) {
            is SignUpResult.RegisterUser.Success -> {
                scene.showSuccess()
            }
            is SignUpResult.RegisterUser.Failure -> handleRegisterUserFailure(result)
        }
    }

    private fun submitCreateUser() {
        scene.showKeyGenerationHolder()

        val newAccount = IncompleteAccount(
                username = model.username.value,
                name = model.fullName.value,
                password = model.password,
                recoveryEmail = if (isSetRecoveryEmail) model.recoveryEmail.value else null
        )

        val req = SignUpRequest.RegisterUser(
                account = newAccount,
                recipientId = model.username.value
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
        scene.initListeners(
                uiObserver = uiObserver
        )
        scene.disableCreateAccountButton()
        return false
    }

    override fun onStop() {
        dataSource.listener = null
        scene.uiObserver = null
    }

    override fun onBackPressed(): Boolean {
        host.exitToScene(SignInParams(), null)
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
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
        fun onBackPressed()
        fun onRegisterUserSuccess()
    }

    companion object {
        val minimumPasswordLength = 8
    }
}
