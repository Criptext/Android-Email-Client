package com.criptext.mail.scenes.settings.recovery_email

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.ChangeEmailParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailRequest
import com.criptext.mail.scenes.settings.recovery_email.data.RecoveryEmailResult
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage

class RecoveryEmailController(
        private val model: RecoveryEmailModel,
        private val scene: RecoveryEmailScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val dataSource: BackgroundWorkManager<RecoveryEmailRequest, RecoveryEmailResult>)
    : SceneController(){

    var lastTimeConfirmationLinkSent: Long
        get() = storage.getLong(KeyValueStorage.StringKey.LastTimeConfirmationLinkSent, 0L)
        set(value) {
            storage.putLong(KeyValueStorage.StringKey.LastTimeConfirmationLinkSent, value)
        }

    override val menuResourceId: Int? = null

    private val signatureUIObserver = object: RecoveryEmailUIObserver{

        override fun onChangeEmailPasswordEnteredOkPressed(password: String) {
            dataSource.submitRequest(RecoveryEmailRequest.CheckPassword(password))
        }

        override fun onChangeEmailNewEmailEnteredOkPressed() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(SettingsParams(), null,true)
        }

        override fun onResendRecoveryLinkPressed() {
            lastTimeConfirmationLinkSent = System.currentTimeMillis()
            scene.onResendLinkTimeSet(RESEND_TIME)
            dataSource.submitRequest(RecoveryEmailRequest.ResendConfirmationLink())
        }

        override fun onChangeRecoveryEmailPressed() {
            host.goToScene(ChangeEmailParams(model.recoveryEmail, model.isEmailConfirmed), false)
        }
    }

    private val dataSourceListener = { result: RecoveryEmailResult ->
        when (result) {
            is RecoveryEmailResult.ResendConfirmationLink -> onResendConfirmationEmail(result)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        model.lastTimeConfirmationLinkSent = lastTimeConfirmationLinkSent
        scene.attachView(signatureUIObserver, keyboardManager, model)
        dataSource.listener = dataSourceListener
        return false
    }

    private fun onResendConfirmationEmail(result: RecoveryEmailResult.ResendConfirmationLink){
        when(result) {
            is RecoveryEmailResult.ResendConfirmationLink.Success -> {
                model.lastTimeConfirmationLinkSent = lastTimeConfirmationLinkSent
                scene.showConfirmationSentDialog()
            }
            is RecoveryEmailResult.ResendConfirmationLink.Failure -> {
                scene.onResendLinkFailed()
                scene.showMessage(UIMessage(R.string.recovery_confirmation_resend_failed))
            }
        }
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        signatureUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    companion object {
        val RESEND_TIME = 300000L
    }
}