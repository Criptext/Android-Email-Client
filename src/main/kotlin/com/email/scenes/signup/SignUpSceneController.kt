package com.email.scenes.signin

import android.view.Menu
import com.email.IHostActivity
import com.email.db.models.User
import com.email.scenes.SceneController
import com.email.scenes.signup.OnRecoveryEmailWarningListener
import com.email.scenes.signup.SignUpSceneModel
import com.email.scenes.signup.data.SignUpRequest
import com.email.scenes.signup.data.SignUpResult

/**
 * Created by sebas on 2/15/18.
 */

class SignUpSceneController(
        private val model: SignUpSceneModel,
        private val scene: SignUpScene,
        private val host : IHostActivity,
        private val dataSource: SignUpDataSource): SceneController() {

    override val menuResourceId: Int?
        get() = null

    val arePasswordsMatching: Boolean
        get() = model.password.equals(model.confirmPassword)

    private val isPasswordErrorShown: Boolean
        get() = model.errors["password"] == true

    private val isUsernameErrorShown: Boolean
        get() = model.errors["username"] == true

    private val isCheckedTermsAndConditions: Boolean
        get() = model.checkTermsAndConditions

    private val fieldsAreEmpty: Boolean
        get() = areFieldsEmpty()

    val isSetRecoveryEmail: Boolean
        get() = model.recoveryEmail.isNotEmpty()


    private fun shouldCreateButtonBeEnabled(): Boolean {
        return !isUsernameErrorShown
                && !isPasswordErrorShown
                && isCheckedTermsAndConditions
                && !fieldsAreEmpty
    }

    private val signUpListener = object : SignUpListener {
        override fun onUsernameChangedListener(text: String) {
            model.username = text
            val isUserAvailable = isUserAvailable()
            scene.toggleUsernameError(userAvailable = isUserAvailable)
            model.errors["username"] = !isUserAvailable

            if(shouldCreateButtonBeEnabled()) {
                scene.enableCreateAccountButton()
            } else {
                scene.disableCreateAccountButton()
            }
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

        override fun onFullNameTextChangeListener(text: String){
            model.fullName = text
            if(shouldCreateButtonBeEnabled()) {
                scene.enableCreateAccountButton()
            }
        }

        override fun onTermsAndConditionsClick(){
            TODO("READ TERMS AND CONDITIONS.")
        }

        override fun onRecoveryEmailTextChangeListener(text: String) {
            model.recoveryEmail = text
        }


        override fun onConfirmPasswordChangedListener(text: String) {
            model.confirmPassword = text
            if (arePasswordsMatching && model.confirmPassword.length > 0) {
                scene.hidePasswordErrors()
                scene.showPasswordSucess()
                model.errors["password"] = false
                if (shouldCreateButtonBeEnabled()) {
                    scene.enableCreateAccountButton()
                }
            } else if (arePasswordsMatching &&
                    model.confirmPassword.length == 0) {
                scene.hidePasswordSucess()
                scene.hidePasswordErrors()
                model.errors["password"] = false
                scene.disableCreateAccountButton()
            } else {
                scene.showPasswordErrors()
                scene.hidePasswordSucess()
                model.errors["password"] = true
                scene.disableCreateAccountButton()
            }
        }

        override fun onPasswordChangedListener(text: String) {
            model.password = text.toString()
            if(arePasswordsMatching && model.password.length > 0) {
                scene.hidePasswordErrors()
                scene.showPasswordSucess()
                model.errors["password"] = false
                if(shouldCreateButtonBeEnabled()) {
                    scene.enableCreateAccountButton()
                }
            } else if(arePasswordsMatching && model.password.length == 0){
                scene.hidePasswordSucess()
                scene.hidePasswordErrors()
                model.errors["password"] = false
                scene.disableCreateAccountButton()
            }
            else {
                scene.showPasswordErrors()
                scene.hidePasswordSucess()
                model.errors["password"] = true
                scene.disableCreateAccountButton()
            }
        }

        override fun onCreateAccountClick() {
            if (!isSetRecoveryEmail) {
                scene.showRecoveryEmailWarningDialog(
                        onRecoveryEmailWarningListener
                )
            } else {
                val user = User(
                        name = model.fullName,
                        email = "",
                        nickname = model.username,
                        id = null
                )
                val req = SignUpRequest.RegisterUser(
                        user = user,
                        password = model.password,
                        recoveryEmail = model.recoveryEmail
                        )
                dataSource.submitRequest(req)
            }
        }

        override fun onBackPressed() {
            host.finishScene()
        }
    }

    private val dataSourceListener = { result: SignUpResult ->
        when (result) {
            is SignUpResult.RegisterUser -> onUserRegistered(result)
        }
    }

    private fun onUserRegistered(result: SignUpResult.RegisterUser) {
        when (result) {
            is SignUpResult.RegisterUser.Success -> scene.showSuccess(result.message)
            is SignUpResult.RegisterUser.Failure -> scene.showError(result.message)
        }
    }

    val onRecoveryEmailWarningListener = object : OnRecoveryEmailWarningListener {
        override fun willAssignRecoverEmail() {
            this@SignUpSceneController.willAssignRecoverEmail()
        }

        override fun denyWillAssignRecoverEmail() {
            this@SignUpSceneController.denyWillAssignRecoverEmail()
        }
    }

    fun willAssignRecoverEmail() {
        TODO("WILL ASSIGN RECOVER EMAIL")
    }

    fun denyWillAssignRecoverEmail() {
        TODO("DENY WILL ASSIGN RECOVER EMAIL")
    }


    fun isUserAvailable(): Boolean {
        return model.username == "sebas"
    }

    override fun onStart() {
        scene.disableCreateAccountButton()

        scene.initListeners(
                signUpListener = signUpListener
        )
        dataSource.listener = dataSourceListener
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    private fun areFieldsEmpty() : Boolean {
        return model.username.isEmpty() ||
                model.fullName.isEmpty() ||
                model.password.isEmpty() ||
                model.confirmPassword.isEmpty() ||
                !model.checkTermsAndConditions
    }

    interface SignUpListener {
            fun onCreateAccountClick()
            fun onPasswordChangedListener(text: String)
            fun onConfirmPasswordChangedListener(text: String)
            fun onUsernameChangedListener(text: String)
            fun onCheckedOptionChanged(state: Boolean)
            fun onTermsAndConditionsClick()
            fun onFullNameTextChangeListener(text: String)
            fun onRecoveryEmailTextChangeListener(text: String)
            fun onBackPressed()
        }
}