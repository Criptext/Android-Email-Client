package com.criptext.mail.scenes.settings

import android.content.Intent
import com.criptext.mail.BaseActivity
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.WebViewActivity
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.params.SignInParams
import com.criptext.mail.scenes.params.SignatureParams
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.devices.DeviceItem
import com.criptext.mail.scenes.settings.labels.LabelWrapperListController
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage

class SettingsController(
        private val model: SettingsModel,
        private val scene: SettingsScene,
        private val host: IHostActivity,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val keyboardManager: KeyboardManager,
        private val dataSource: BackgroundWorkManager<SettingsRequest, SettingsResult>)
    : SceneController(){

    override val menuResourceId: Int? = null

    private val labelWrapperListController = LabelWrapperListController(model, scene.getLabelListView())

    private val dataSourceListener = { result: SettingsResult ->
        when (result) {
            is SettingsResult.ChangeContactName -> onContactNameChanged(result)
            is SettingsResult.GetCustomLabels -> onGetCustomLabels(result)
            is SettingsResult.CreateCustomLabel -> onCreateCustomLabels(result)
            is SettingsResult.Logout -> onLogout(result)
        }
    }

    private val settingsUIObserver = object: SettingsUIObserver{

        override fun onCustomLabelNameAdded(labelName: String) {
            dataSource.submitRequest(SettingsRequest.CreateCustomLabel(labelName))
            keyboardManager.hideKeyboard()
        }

        override fun onProfileNameClicked() {
            scene.showProfileNameDialog(model.fullName)
        }

        override fun onPrivacyPoliciesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/privacy")
            context.startActivity(intent)
        }
        override fun onTermsOfServiceClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/terms")
            context.startActivity(intent)
        }
        override fun onOpenSourceLibrariesClicked() {
            val context = (host as BaseActivity)
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", "https://criptext.com/open-source-libraries-android")
            context.startActivity(intent)
        }
        override fun onLogoutClicked() {
            scene.showLogoutDialog()
        }

        override fun onLogoutConfirmedClicked() {
            scene.showLoginOutDialog()
            dataSource.submitRequest(SettingsRequest.Logout())
        }
        override fun onBackButtonPressed() {
            host.finishScene()
        }

        override fun onSignatureOptionClicked() {
            host.goToScene(SignatureParams(activeAccount.recipientId), true)
        }

        override fun onToggleLabelSelection(label: LabelWrapper) {
            dataSource.submitRequest(SettingsRequest.ChangeVisibilityLabel(label.id, label.isSelected))
        }

        override fun onCreateLabelClicked() {
            scene.showCreateLabelDialog(keyboardManager)
        }

        override fun onProfileNameChanged(fullName: String) {
            keyboardManager.hideKeyboard()
            activeAccount.updateFullName(storage, fullName)
            model.fullName = fullName
            dataSource.submitRequest(SettingsRequest.ChangeContactName(
                    fullName = fullName,
                    recipientId = activeAccount.recipientId
            ))
        }
    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        dataSource.listener = dataSourceListener
        model.fullName = activeAccount.name
        model.signature = activeAccount.signature
        if(model.labels.isEmpty()) {
            dataSource.submitRequest(SettingsRequest.GetCustomLabels())
        }
        return false
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    private fun onContactNameChanged(result: SettingsResult.ChangeContactName){
        when(result) {
            is SettingsResult.ChangeContactName.Success -> {
                scene.showMessage(UIMessage(R.string.profile_name_updated))
            }
            is SettingsResult.ChangeContactName.Failure -> {
                scene.showMessage(UIMessage(R.string.error_updating_account))
            }
        }
    }

    private fun onCreateCustomLabels(result: SettingsResult.CreateCustomLabel){
        when(result) {
            is SettingsResult.CreateCustomLabel.Success -> {
                val labelWrapper = LabelWrapper(result.label)
                labelWrapper.isSelected = true
                labelWrapperListController.addNew(labelWrapper)
            }
            is SettingsResult.CreateCustomLabel.Failure -> {
                scene.showMessage(UIMessage(R.string.error_creating_labels))
                host.finishScene()
            }
        }
    }

    private fun onGetCustomLabels(result: SettingsResult.GetCustomLabels){
        when(result) {
            is SettingsResult.GetCustomLabels.Success -> {
                model.labels.addAll(result.labels.map {
                    val labelWrapper = LabelWrapper(it)
                    labelWrapper.isSelected = it.visible
                    labelWrapper
                })
                //This is done until we get devices from server
                model.devices.add(DeviceItem(activeAccount.deviceId.toLong(), DeviceUtils.getDeviceName()))
                scene.attachView(
                        name = activeAccount.name,
                        model = model,
                        settingsUIObserver = settingsUIObserver)
            }
            is SettingsResult.GetCustomLabels.Failure -> {
                scene.showMessage(UIMessage(R.string.error_getting_labels))
                host.finishScene()
            }
        }
    }

    private fun onLogout(result: SettingsResult.Logout){
        when(result) {
            is SettingsResult.Logout.Success -> {
                host.goToScene(SignInParams(), false)
            }
            is SettingsResult.Logout.Failure -> {
                scene.showMessage(UIMessage(R.string.error_login_out))
            }
        }
    }

}