package com.criptext.mail.scenes.webview

import android.content.ActivityNotFoundException
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.scenes.search.data.SearchResult
import com.criptext.mail.scenes.webview.ui.WebViewUIObserver
import com.criptext.mail.utils.AccountUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import com.criptext.mail.utils.ui.data.DialogResult
import com.criptext.mail.utils.ui.data.TransitionAnimationData

class WebViewSceneController(private val scene: WebViewScene,
                             private val model: WebViewSceneModel,
                             private val host: IHostActivity,
                             private val activeAccount: ActiveAccount,
                             storage: KeyValueStorage,
                             private val generalDataSource: GeneralDataSource)
    : SceneController(){

    override val menuResourceId: Int?
        get() = when {
            model.isOnAdmin -> null
            else -> R.menu.menu_webview
        }

    override fun onOptionsItemSelected(itemId: Int) {
        when (itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.ac_open -> {
                if(!model.mUrl.isNullOrEmpty()) {
                    host.launchExternalActivityForResult(
                            ExternalActivityParams.OpenExternalBrowser(model.mUrl!!)
                    )
                }
            }
        }
    }

    private val dataSourceListener = { result: SearchResult ->

    }

    private val chromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            // make sure there is no existing message
            if (model.mUploadMessage != null) {
                model.mUploadMessage!!.onReceiveValue(null)
                model.mUploadMessage = null
            }

            model.mUploadMessage = filePathCallback

            try {
                host.launchExternalActivityForResult(ExternalActivityParams.OpenBrowserFilePicker())
            } catch (e: ActivityNotFoundException) {
                model.mUploadMessage = null
                scene.showError(UIMessage(R.string.unknown_error, arrayOf(e.toString())))
                return false
            }

            return true
        }
    }

    private val observer = object :WebViewUIObserver{
        override fun onPageStartedLoading(url: String?) {
            if(model.isOnAdmin) {
                val title = if(AccountUtils.isPlus(activeAccount.type)) {
                    UIMessage(R.string.billing_settings_title)
                } else {
                    UIMessage(R.string.title_web_view_upgrade_to_plus)
                }
                scene.setTitle(title)
            }
        }

        override fun onPageFinishedLoading(url: String) {

        }

        override fun onBrowserDownload(url: String, contentDisposition: String) {
            host.launchExternalActivityForResult(ExternalActivityParams.BrowserHandleDownload(url, contentDisposition))
            if (url == model.mUrl) {
                onBackPressed()
            }
        }

        override fun onJSInterfaceClose() {
            host.goToScene(
                    params = MailboxParams(),
                    deletePastIntents = true,
                    keep = false,
                    activityMessage = null,
                    animationData = TransitionAnimationData(
                            forceAnimation = true,
                            enterAnim = 0,
                            exitAnim = R.anim.slide_out_right
                    )
            )
        }

        override fun onUrlChanged(newUrl: String) {
            model.mUrl = newUrl
        }

        override fun onBackButtonPressed() {
            onBackPressed()
        }

        override fun onGeneralOkButtonPressed(result: DialogResult) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onGeneralCancelButtonPressed(result: DialogResult) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onOkButtonPressed(password: String) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onCancelButtonPressed() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSnackbarClicked() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onStart(activityMessage: ActivityMessage?): Boolean {
        model.isOnAdmin = model.mUrl?.contains(ADMIN_URL) ?: false
        scene.attachView(
                observer, activeAccount, model, chromeClient
        )
        return handleActivityMessage(activityMessage)
    }

    private fun handleActivityMessage(activityMessage: ActivityMessage?): Boolean {
        if (activityMessage is ActivityMessage.BrowserAddAttachment) {
            if (model.mUploadMessage == null) return true
            model.mUploadMessage!!.onReceiveValue(activityMessage.uris)
            model.mUploadMessage = null
            return true
        } else if (activityMessage is ActivityMessage.ComesFromMailbox){
            model.comesFromMailbox = true
        }
        return false
    }

    override fun onResume(activityMessage: ActivityMessage?): Boolean {
        return false
    }

    override fun onPause() {

    }

    override fun onStop() {

    }

    override fun onNeedToSendEvent(event: Int) {
        generalDataSource.submitRequest(GeneralRequest.UserEvent(event))
    }

    override fun onBackPressed(): Boolean {
        if(model.comesFromMailbox){
            host.finishScene(ActivityMessage.RefreshUI())
        } else {
            host.finishScene()
        }
        return true
    }

    override fun onMenuChanged(menu: IHostActivity.IActivityMenu) {}

    override fun requestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    }

    companion object{
        const val userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/LMY48B) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36"

        const val HELP_DESK_URL = "https://criptext.atlassian.net/servicedesk/customer/portals"
        const val ADMIN_URL = "https://admin.criptext.com/?#/account/billing"
    }
}