package com.criptext.mail.scenes.settings.changepassword

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.settings.changepassword.data.ChangePasswordRequest
import com.criptext.mail.scenes.settings.changepassword.data.ChangePasswordResult
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.validation.FormInputState

class ChangePasswordController(
        private val model: ChangePasswordModel,
        private val scene: ChangePasswordScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val generalDataSource: BackgroundWorkManager<GeneralRequest, GeneralResult>,
        private val dataSource: BackgroundWorkManager<ChangePasswordRequest, ChangePasswordResult>)
    : SceneController(){

    val arePasswordsMatching: Boolean
        get() = model.passwordText == model.confirmPasswordText

    override val menuResourceId: Int? = null

    private val generalDataSourceListener: (GeneralResult) -> Unit = { result ->
        when(result) {
            is GeneralResult.ResetPassword -> onResetPassword(result)
        }
    }

    private val changePasswordUIObserver = object: ChangePasswordUIObserver{
        override fun onForgotPasswordPressed() {
            generalDataSource.submitRequest(GeneralRequest.ResetPassword())
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
        }

        override fun onOldPasswordChangedListener(password: String) {
            model.oldPasswordText = password
            if(model.oldPasswordText != model.lastUsedPassword)
                scene.showOldPasswordError(null)
            else
                scene.showOldPasswordError(UIMessage(R.string.password_enter_error))
        }

        override fun onPasswordChangedListener(password: String) {
            model.passwordText = password
            if(model.confirmPasswordText.isNotEmpty())
                checkPasswords(Pair(model.passwordText, model.confirmPasswordText))
        }

        override fun onConfirmPasswordChangedListener(confirmPassword: String) {
            model.confirmPasswordText = confirmPassword
            checkPasswords(Pair(model.confirmPasswordText, model.passwordText))
        }

        override fun onChangePasswordButtonPressed() {
            keyboardManager.hideKeyboard()
            model.lastUsedPassword = model.oldPasswordText
            dataSource.submitRequest(ChangePasswordRequest.ChangePassword(model.oldPasswordText, model.confirmPasswordText))
        }
    }

    private val dataSourceListener = { result: ChangePasswordResult ->
        when (result) {
            is ChangePasswordResult.ChangePassword -> onChangePasswordResult(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        scene.attachView(changePasswordUIObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        generalDataSource.listener = generalDataSourceListener
        return false
    }

    private fun checkPasswords(passwords: Pair<String, String>) {
        if (arePasswordsMatching && passwords.first.length >= minimumPasswordLength) {
            scene.showPasswordDialogError(null)
            model.passwordState = FormInputState.Valid()
            if (model.passwordState is FormInputState.Valid)
                scene.toggleChangePasswordButton(true)
        } else if (arePasswordsMatching && passwords.first.isEmpty()) {
            scene.showPasswordDialogError(null)
            model.passwordState = FormInputState.Unknown()
            scene.toggleChangePasswordButton(false)
        } else if (arePasswordsMatching && passwords.first.length < minimumPasswordLength) {
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

    private fun onChangePasswordResult(result: ChangePasswordResult.ChangePassword){
        when(result) {
            is ChangePasswordResult.ChangePassword.Success -> {
                scene.showMessage(UIMessage(R.string.change_password_success))
                host.finishScene()
            }
            is ChangePasswordResult.ChangePassword.Failure -> {
                scene.showOldPasswordError(UIMessage(R.string.password_enter_error))
            }
        }
    }

    private fun onResetPassword(result: GeneralResult.ResetPassword){
        when(result) {
            is GeneralResult.ResetPassword.Success -> {
                scene.showForgotPasswordDialog(result.email)
            }
            is GeneralResult.ResetPassword.Failure -> {
                scene.showMessage(result.message)
            }
        }
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        changePasswordUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        const val minimumPasswordLength = 8
    }
}