package com.criptext.mail.scenes.settings.signature

import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage

class SignatureController(
        private val model: SignatureModel,
        private val scene: SignatureScene,
        private val host: IHostActivity,
        private val keyboardManager: KeyboardManager,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val dataSource: BackgroundWorkManager<SettingsRequest, SettingsResult>)
    : SceneController(){

    override val menuResourceId: Int? = R.menu.menu_signature

    private val signatureUIObserver = object: SignatureUIObserver{
        override fun onBackButtonPressed() {
            if(needToUpdateSignature()) {
                updateSignature()
            }
            keyboardManager.hideKeyboard()
            host.finishScene()
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        model.signature = activeAccount.signature
        scene.attachView(signatureUIObserver, activeAccount.signature, keyboardManager)
        return false
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        if(needToUpdateSignature()) {
            updateSignature()
        }
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {
        when(itemId){
            R.id.ac_done -> {
                updateSignature()
                keyboardManager.hideKeyboard()
                host.finishScene()
            }
        }
    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    private fun needToUpdateSignature():Boolean = model.signature != scene.getSignature()

    private fun updateSignature(){
        activeAccount.updateSignature(storage, scene.getSignature())
    }

}