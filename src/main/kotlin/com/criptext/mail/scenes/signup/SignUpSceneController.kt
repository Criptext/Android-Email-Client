package com.criptext.mail.scenes.signup

import com.criptext.mail.*
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.RunnableThrottler
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.CustomizeParams
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.params.WebViewParams
import com.criptext.mail.scenes.signup.data.SignUpRequest
import com.criptext.mail.scenes.signup.data.SignUpResult
import com.criptext.mail.scenes.signup.holders.SignUpLayoutState
import com.criptext.mail.scenes.webview.WebViewSceneController
import com.criptext.mail.utils.*
import com.criptext.mail.utils.ui.data.TransitionAnimationData
import com.criptext.mail.validation.*
import java.util.*

/**
 * Created by sebas on 2/15/18.
 */

class SignUpSceneController(
        private val model: SignUpSceneModel,
        private val scene: SignUpScene,
        private val keyboardManager: KeyboardManager,
        private val host : IHostActivity,
        private val storage: KeyValueStorage,
        private val dataSource: BackgroundWorkManager<SignUpRequest, SignUpResult>,
        private val runnableThrottler: RunnableThrottler): SceneController(host, null, storage) {

    override val menuResourceId: Int?
        get() = null

    val arePasswordsMatching: Boolean
        get() = model.password == model.confirmPassword

    val isSetRecoveryEmail: Boolean
        get() = model.recoveryEmail.value.isNotEmpty()

    private val isCheckedTermsAndConditions: Boolean
        get() = model.checkTermsAndConditions

    private val uiObserver: SignUpUIObserver = object : SignUpUIObserver {
        override fun onNextButtonPressed() {
            scene.setSubmitButtonState(ProgressButtonState.waiting)
            when (model.state) {
                is SignUpLayoutState.Name -> {
                    model.state = SignUpLayoutState.EmailHandle(model.username.value)
                    resetLayout()
                }
                is SignUpLayoutState.EmailHandle -> {
                    model.state = SignUpLayoutState.Password(model.password)
                    resetLayout()
                }
                is SignUpLayoutState.Password -> {
                    model.state = SignUpLayoutState.ConfirmPassword(model.confirmPassword)
                    resetLayout()
                }
                is SignUpLayoutState.ConfirmPassword -> {
                    model.state = SignUpLayoutState.RecoveryEmail(model.recoveryEmail.value)
                    resetLayout()
                }
                is SignUpLayoutState.RecoveryEmail -> {
                    model.state = SignUpLayoutState.TermsAndConditions()
                    resetLayout()
                }
                is SignUpLayoutState.TermsAndConditions -> {
                    scene.showGeneratingKeys(true)
                    submitCreateUser()
                }
            }
        }

        override fun onUsernameChangedListener(text: String) {
            val newUsername = if (text.isEmpty()) {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
                model.username.copy(state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateUsernameOnly(text)
                when (userInput) {
                    is FormData.Valid -> {
                        scene.setSubmitButtonState(ProgressButtonState.waiting)
                        runnableThrottler.push(Runnable {
                            val newRequest = SignUpRequest.CheckUserAvailability(userInput.value)
                            dataSource.submitRequest(newRequest)
                        })

                        // Only the server can truly validate this
                        model.username.copy(value = userInput.value,
                                            state = FormInputState.Unknown())
                    }

                    is FormData.Error -> {
                        scene.setSubmitButtonState(ProgressButtonState.disabled)
                        model.username.copy(value = text,
                                            state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.username = newUsername
            scene.setInputState(model.state, newUsername.state)
        }

        override fun onFullNameTextChangeListener(text: String){
            val newFullName = if (text.isEmpty()) {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
                model.fullName.copy(state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateFullName(text)
                when (userInput) {
                    is FormData.Valid -> {
                        scene.setSubmitButtonState(ProgressButtonState.enabled)
                        model.fullName.copy(value = userInput.value,
                                            state = FormInputState.Valid())
                    }

                    is FormData.Error -> {
                        scene.setSubmitButtonState(ProgressButtonState.disabled)
                        model.fullName.copy(value = text,
                                            state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.fullName = newFullName
            scene.setInputState(model.state, newFullName.state)
        }

        override fun onRecoveryEmailTextChangeListener(text: String) {
            val newRecoveryEmail = if (text.isEmpty()) {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
                TextInput(value = text, state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateEmailAddress(text)
                when (userInput) {
                    is FormData.Valid -> {
                        scene.setSubmitButtonState(ProgressButtonState.waiting)
                        runnableThrottler.push(Runnable {
                            val newRequest = SignUpRequest.CheckRecoveryEmailAvailability(model.username.value, userInput.value)
                            dataSource.submitRequest(newRequest)
                        })

                        TextInput(value = userInput.value,
                                state = FormInputState.Unknown())
                    }

                    is FormData.Error -> {
                        scene.setSubmitButtonState(ProgressButtonState.disabled)
                        TextInput(value = text,
                                state = FormInputState.Error(userInput.message))
                    }
                }
            }
            model.recoveryEmail = newRecoveryEmail
            scene.setInputState(model.state, newRecoveryEmail.state)
        }

        override fun onCheckedOptionChanged(state: Boolean) {
            model.checkTermsAndConditions = state
            if(state){
                scene.setSubmitButtonState(ProgressButtonState.enabled)
            } else {
                scene.setSubmitButtonState(ProgressButtonState.disabled)
            }
        }

        override fun onTermsAndConditionsClick(){
            host.goToScene(
                    params = WebViewParams(
                            url = "https://criptext.com/${Locale.getDefault().language}/terms",
                            title = null
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

        override fun onContactSupportClick() {
            host.goToScene(
                    params = WebViewParams(
                            url = Hosts.HELP_DESK_URL,
                            title = null
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

        private fun checkPasswords(passwords: Pair<String, String>) {
            when(model.state){
                is SignUpLayoutState.Password -> {
                    scene.setPasswordCheck(
                            isNotUsername = model.username.value != passwords.first,
                            isAtLeastEightChars = passwords.first.length >= minimumPasswordLength
                    )
                }
                is SignUpLayoutState.ConfirmPassword -> {
                    val inputState = when {
                        passwords.first.isEmpty() -> {
                            FormInputState.Unknown()
                        }
                        passwords.first == passwords.second -> {
                            FormInputState.Valid()
                        }
                        else -> {
                            FormInputState.Error(UIMessage(R.string.password_mismatch_error))
                        }
                    }
                    scene.setConfirmPasswordCheck(
                            passwordMatches = inputState
                    )
                }
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
            keyboardManager.hideKeyboard()
            this@SignUpSceneController.submitCreateUser()
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
            is SignUpResult.CheckRecoveryEmailAvailability -> onCheckedRecoveryEmailAvailability(result)
        }
    }

    private fun onCheckedUsernameAvailability(result: SignUpResult.CheckUsernameAvailability) {
        when (result) {
            is SignUpResult.CheckUsernameAvailability.Success -> {
                if (result.isAvailable) {
                    scene.setInputState(model.state, FormInputState.Valid())
                    scene.setSubmitButtonState(ProgressButtonState.enabled)
                } else {
                    val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
                    model.username = model.username.copy(state = newState)
                    scene.setInputState(model.state, newState)
                    scene.setSubmitButtonState(ProgressButtonState.disabled)
                }
            }
            is SignUpResult.CheckUsernameAvailability.Failure -> {
                val newState = FormInputState.Error(UIMessage(R.string.taken_username_error))
                model.username = model.username.copy(state = newState)
                scene.setInputState(model.state, newState)
                scene.setSubmitButtonState(ProgressButtonState.disabled)
            }
        }
    }

    private fun onCheckedRecoveryEmailAvailability(result: SignUpResult.CheckRecoveryEmailAvailability) {
        when (result) {
            is SignUpResult.CheckRecoveryEmailAvailability.Success -> {
                scene.setInputState(model.state, FormInputState.Valid())
                scene.setSubmitButtonState(ProgressButtonState.enabled)
            }
            is SignUpResult.CheckRecoveryEmailAvailability.Failure -> {
                val newState = FormInputState.Error(result.errorMessage)
                model.username = model.username.copy(state = newState)
                scene.setInputState(model.state, newState)
                scene.setSubmitButtonState(ProgressButtonState.disabled)
            }
        }
    }

    private fun handleRegisterUserFailure(result: SignUpResult.RegisterUser.Failure) {
        scene.showGeneratingKeys(false)
        scene.showError(result.message)
    }
    private fun onUserRegistered(result: SignUpResult.RegisterUser) {
        when (result) {
            is SignUpResult.RegisterUser.Success -> {
                host.goToScene(
                    params = CustomizeParams(model.recoveryEmail.value),
                    keep = false,
                    deletePastIntents = true,
                    activityMessage = null
                )
            }
            is SignUpResult.RegisterUser.Failure -> handleRegisterUserFailure(result)
        }
    }

    private fun submitCreateUser() {
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

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        scene.initLayout(
                model,
                uiObserver
        )
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

    override fun onNeedToSendEvent(event: Int) {
        return
    }

    private fun resetLayout(buttonState: ProgressButtonState = ProgressButtonState.disabled) {
        scene.setSubmitButtonState(buttonState)
        scene.initLayout(model, uiObserver)
    }

    override fun onBackPressed(): Boolean {
        return when (model.state) {
            is SignUpLayoutState.Name -> {
                if(model.isMultiple) {
                    host.finishScene()
                    false
                } else {
                    true
                }
            }
            is SignUpLayoutState.EmailHandle -> {
                model.state = SignUpLayoutState.Name(model.fullName.value)
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is SignUpLayoutState.Password -> {
                model.state = SignUpLayoutState.EmailHandle(model.username.value)
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is SignUpLayoutState.ConfirmPassword -> {
                model.state = SignUpLayoutState.Password(model.password)
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is SignUpLayoutState.RecoveryEmail -> {
                model.state = SignUpLayoutState.ConfirmPassword(model.confirmPassword)
                resetLayout(ProgressButtonState.enabled)
                false
            }
            is SignUpLayoutState.TermsAndConditions -> {
                model.state = SignUpLayoutState.RecoveryEmail(model.recoveryEmail.value)
                resetLayout(ProgressButtonState.enabled)
                false
            }
        }
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
        fun onNextButtonPressed()
        fun onContactSupportClick()
        fun onBackPressed()
        fun onProgressHolderFinish()
    }

    companion object {
        val minimumPasswordLength = 8
    }
}
