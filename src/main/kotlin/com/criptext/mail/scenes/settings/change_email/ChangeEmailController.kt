package com.criptext.mail.scenes.settings.change_email

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.RecoveryEmailParams
import com.criptext.mail.scenes.settings.change_email.data.ChangeEmailRequest
import com.criptext.mail.scenes.settings.change_email.data.ChangeEmailResult
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData
import com.criptext.mail.validation.FormInputState
import com.criptext.mail.validation.TextInput

class ChangeEmailController(
        private val model: ChangeEmailModel,
        private val scene: ChangeEmailScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val dataSource: BackgroundWorkManager<ChangeEmailRequest, ChangeEmailResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val changeEmailUIObserver = object: ChangeEmailUIObserver{

        override fun onEnterPasswordOkPressed(password: String) {
            scene.dialogToggleLoad(true)
            dataSource.submitRequest(ChangeEmailRequest.ChangeRecoveryEmail(password, model.newRecoveryEmail.value))
        }

        override fun onChangeButtonPressed(text: String) {
            scene.showEnterPasswordDialog()
        }

        override fun onRecoveryEmailTextChanged(text: String) {
            val newRecoveryEmail = if (text.isEmpty()) {
                TextInput(value = text, state = FormInputState.Unknown())
            } else {
                val userInput = AccountDataValidator.validateRecoveryEmailAddress(text)
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
            model.newRecoveryEmail = newRecoveryEmail
            scene.setRecoveryEmailState(newRecoveryEmail.state)
            toggleChangeEmailButton()
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(RecoveryEmailParams(model.isConfirmed, model.recoveryEmail),
                    null, true)
        }
    }

    private fun toggleChangeEmailButton() {
        if(model.newRecoveryEmail.state !is FormInputState.Error
                && model.newRecoveryEmail.value != model.recoveryEmail) {
            scene.enableChangeButton()
        } else {
            scene.disableChangeButton()
        }

    }

    private val dataSourceListener = { result: ChangeEmailResult ->
        when (result) {
            is ChangeEmailResult.ChangeRecoveryEmail -> onChangeRecoveryEmail(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        scene.attachView(changeEmailUIObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        return false
    }

    private fun onChangeRecoveryEmail(result: ChangeEmailResult.ChangeRecoveryEmail){
        when(result) {
            is ChangeEmailResult.ChangeRecoveryEmail.Success -> {
                model.recoveryEmail = model.newRecoveryEmail.value
                model.isConfirmed = false
                scene.enterPasswordDialogDismiss()
                scene.showMessage(UIMessage(R.string.recovery_email_has_changed))
            }
            is ChangeEmailResult.ChangeRecoveryEmail.Failure -> {
                scene.enterPasswordDialogError(result.message)
                scene.dialogToggleLoad(false)
            }
        }
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        changeEmailUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
}