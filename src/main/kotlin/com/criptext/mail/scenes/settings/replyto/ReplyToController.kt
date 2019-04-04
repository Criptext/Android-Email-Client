package com.criptext.mail.scenes.settings.replyto

import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.ProfileParams
import com.criptext.mail.scenes.params.SettingsParams
import com.criptext.mail.scenes.settings.replyto.data.ReplyToRequest
import com.criptext.mail.scenes.settings.replyto.data.ReplyToResult
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.validation.AccountDataValidator
import com.criptext.mail.validation.FormData

class ReplyToController(
        private val model: ReplyToModel,
        private val scene: ReplyToScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val dataSource: BackgroundWorkManager<ReplyToRequest, ReplyToResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val dataSourceListener = { result: ReplyToResult ->
        when (result) {
            is ReplyToResult.SetReplyToEmail -> onReplyEmailChanged(result)
        }
    }

    private val replyToUIObserver = object: ReplyToUIObserver{
        override fun onTurnOffReplyTo() {
            model.newReplyToEmail = ""
            scene.clearTextBox()
            dataSource.submitRequest(ReplyToRequest.SetReplyToEmail(model.newReplyToEmail, false))
        }

        override fun onRecoveryChangeButonPressed() {
            scene.disableSaveButton()
            dataSource.submitRequest(ReplyToRequest.SetReplyToEmail(model.newReplyToEmail, true))
        }

        override fun onRecoveryEmailChanged(text: String) {
            model.newReplyToEmail = text
            val userInput = AccountDataValidator.validateEmailAddress(model.newReplyToEmail)
            when (userInput) {
                is FormData.Valid -> {
                    if (!text.isEmpty() && text != model.userData.replyToEmail) {
                        scene.setEmailError(null)
                        scene.enableSaveButton()
                    } else {
                        scene.disableSaveButton()
                    }
                }
                is FormData.Error -> {
                    scene.disableSaveButton()
                    scene.setEmailError(userInput.message)
                }
            }
        }

        override fun onBackButtonPressed() {
            keyboardManager.hideKeyboard()
            host.exitToScene(ProfileParams(model.userData), null, false)
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        scene.attachView(replyToUIObserver, model.userData.replyToEmail ?: "", keyboardManager)
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    private fun onReplyEmailChanged(result: ReplyToResult.SetReplyToEmail){
        when(result) {
            is ReplyToResult.SetReplyToEmail.Success -> {
                if(result.enabled) {
                    model.userData.replyToEmail = result.replyToEmail
                    scene.showMessage(UIMessage(R.string.reply_to_email_has_changed))
                }else {
                    model.userData.replyToEmail = null
                    scene.enableSaveButton()
                    scene.showMessage(UIMessage(R.string.reply_to_email_removed))
                }
            }
            is ReplyToResult.SetReplyToEmail.Failure -> {
                scene.enableSaveButton()
                scene.showMessage(result.message)
            }
        }
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        replyToUIObserver.onBackButtonPressed()
        return false
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
}