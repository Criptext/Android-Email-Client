package com.criptext.mail.scenes.webview

import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.RelativeLayout
import android.widget.TextView
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.getLocalizedUIMessage
import droidninja.filepicker.FilePickerConst


class WebViewActivity : BaseActivity() {

    override val layoutId = R.layout.activity_web_view
    override val toolbarId = R.id.web_view_toolbar

    private val webViewCriptext: WebView by lazy { findViewById<WebView>(R.id.webViewCriptext) }
    private var browserName: String? = null

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val appDB = AppDatabase.getAppDatabase(this)
        val model = receivedModel as WebViewSceneModel
        browserName = model.browserName
        val scene = WebViewScene.WebViewSceneView(findViewById(R.id.rootView), KeyboardManager(this))
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val storage = KeyValueStorage.SharedPrefs(this.applicationContext)
        return WebViewSceneController(
                scene = scene,
                model = model,
                host = this,
                storage = storage,
                activeAccount = activeAccount,
                clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager,
                generalDataSource = GeneralDataSource(
                        storage = storage,
                        httpClient = HttpClient.Default(),
                        db = appDB,
                        activeAccount = activeAccount,
                        eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                        filesDir = this.filesDir,
                        runner = AsyncTaskWorkRunner(),
                        signalClient = null,
                        cacheDir = this.cacheDir
                ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webViewCriptext.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webViewCriptext.restoreState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(browserName != null){
            menu.findItem(R.id.ac_open)?.title = this.getLocalizedUIMessage(UIMessage(
                    R.string.web_view_open_in_chrome, arrayOf(browserName!!)
            ))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  intent: Intent?) {
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            setActivityMessage(ActivityMessage.BrowserAddAttachment(WebChromeClient.FileChooserParams.parseResult(resultCode, intent)))
        }
    }
}
