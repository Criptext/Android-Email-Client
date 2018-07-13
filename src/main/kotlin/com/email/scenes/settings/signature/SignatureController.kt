package com.email.scenes.settings.signature

import com.email.scenes.SceneController
import com.email.scenes.settings.data.SettingsResult
import com.email.IHostActivity
import com.email.R
import com.email.bgworker.BackgroundWorkManager
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.scenes.ActivityMessage
import com.email.scenes.settings.data.SettingsRequest
import com.email.utils.KeyboardManager
import com.email.utils.UIMessage

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