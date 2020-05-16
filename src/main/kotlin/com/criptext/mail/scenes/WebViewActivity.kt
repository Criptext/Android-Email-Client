package com.criptext.mail.scenes

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.CriptextJavaScriptInterface
import droidninja.filepicker.FilePickerConst
import org.w3c.dom.Text


class WebViewActivity : AppCompatActivity() {
    val userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/LMY48B) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36"

    val webViewCriptext: WebView by lazy { findViewById<WebView>(R.id.webViewCriptext) }
    val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progressBar) }
    val errorLayout: LinearLayout by lazy { findViewById<LinearLayout>(R.id.errorLayout) }
    var mUrl: String? = null
    var browserName: String? = null
    private var mUploadMessage: ValueCallback<Array<Uri>>? = null

    private var isOnAdmin = false
    private var initialAccountType: AccountTypes? = null

    private val client = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            mUrl = url
            return loadUrlOnView(mUrl!!)
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest): Boolean {
            mUrl = request.url.toString()
            return loadUrlOnView(request.url.toString())
        }

        override fun onPageFinished(view: WebView, url: String) {
            progressBar.visibility = View.GONE
            if(isOnAdmin){
                webViewCriptext.settings.loadsImagesAutomatically = true
                if(AccountUtils.isPlus(initialAccountType!!)) {
                    supportActionBar?.title = view.context
                            .getLocalizedUIMessage(UIMessage(R.string.billing_settings_title))
                } else {
                    supportActionBar?.title = view.context
                            .getLocalizedUIMessage(UIMessage(R.string.title_web_view_upgrade_to_plus))
                }
            } else {
                supportActionBar?.title = view.title
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            webViewCriptext.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
        }

        private fun loadUrlOnView(url: String): Boolean {
            if (URLUtil.isNetworkUrl(url)) {
                return false
            }
            return true
        }
    }

    private val chromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            // make sure there is no existing message
            if (mUploadMessage != null) {
                mUploadMessage!!.onReceiveValue(null)
                mUploadMessage = null
            }

            mUploadMessage = filePathCallback

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                type = "image/*"
            }
            try {
                this@WebViewActivity.startActivityForResult(intent, FilePickerConst.REQUEST_CODE_PHOTO)
            } catch (e: ActivityNotFoundException) {
                mUploadMessage = null
                Toast.makeText(this@WebViewActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
                return false
            }

            return true
        }
    }

    private val downloadListener = { url: String, _: String, contentDisposition: String,
                                     _: String, _: Long ->

        if (WebViewUtils.hasSDPermissionsWeb(this)) {
            if (url.contains("blob:")) {
                injectRetrieveBlobData(url)
            } else {
                var filename = intent.getStringExtra("name") ?: "noname"
                if (contentDisposition != "") {
                    val params = contentDisposition.split(";")
                    for (param in params) {
                        if (param.contains("filename")) {
                            filename = param.replace("filename=", "").replace("\"", "")
                            break
                        }
                    }
                }
                DownloadHelper.createDownloader(this, url, filename)
                if (url == mUrl) {
                    Toast.makeText(this, "Your download for $filename has started",
                            Toast.LENGTH_LONG).show()
                    onBackPressed()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        setupThemeForError()
        setupWebView()
        setupActionBar()
        val accountType = intent.getIntExtra("accountType", -1)
        if(accountType != -1)
            initialAccountType = AccountTypes.fromInt(accountType)
        tryToRestoreState(savedInstanceState)
    }

    private fun setupThemeForError(){
        val backColor = intent.getIntExtra("colorBackground", -1)
        if(backColor != -1) {
            val textColor = intent.getIntExtra("colorText1", -1)
            val textColor2 = intent.getIntExtra("colorText2", -1)
            this.findViewById<RelativeLayout>(R.id.rootView).setBackgroundColor(backColor)
            this.findViewById<TextView>(R.id.title).setTextColor(textColor)
            this.findViewById<TextView>(R.id.message).setTextColor(textColor2)
        }
    }

    private fun setupWebView() {
        webViewCriptext.webChromeClient = chromeClient
        webViewCriptext.settings.javaScriptEnabled = true
        webViewCriptext.settings.userAgentString = userAgent
        webViewCriptext.webViewClient = client
        webViewCriptext.setDownloadListener(downloadListener)

        val javascriptInterface = CriptextJavaScriptInterface(this, intent.getStringExtra("name") ?: "noname")
        webViewCriptext.addJavascriptInterface(javascriptInterface, "criptoBridge")
    }

    private fun setupActionBar() {
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.exit)
    }

    private fun tryToRestoreState(savedInstanceState: Bundle?) {
        mUrl = intent.getStringExtra("url")
        isOnAdmin = mUrl?.contains(BaseActivity.ADMIN_URL) ?: false
        webViewCriptext.loadUrl(mUrl)
    }

    private fun injectRetrieveBlobData(url : String){
        val sb = StringBuilder()
        sb.append("var xhr = new XMLHttpRequest();")
        sb.append("xhr.open('GET', '$url', true);")
        sb.append("xhr.responseType = 'arraybuffer';")
        sb.append("xhr.onload = function(e) {")
        sb.append("if (this.status == 200) {")
        sb.append("var uInt8Array = new Uint8Array(this.response);")
        sb.append("var i = uInt8Array.length;")
        sb.append("var binaryString = new Array(i);")
        sb.append("while (i--){")
        sb.append("binaryString[i] = String.fromCharCode(uInt8Array[i]);")
        sb.append("};")
        sb.append("var data = binaryString.join('');")
        sb.append("var base64 = window.btoa(data);")
        sb.append("CriptextSecureEmail.retrieveData(base64);")
        sb.append("};")
        sb.append("};")
        sb.append("xhr.send();")

        webViewCriptext.loadUrl("javascript:$sb")
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
        if(isOnAdmin) return false
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_webview, menu)
        if(!mUrl.isNullOrEmpty()) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))
            val resolveInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY) ?: return super.onCreateOptionsMenu(menu)
            browserName = resolveInfo.activityInfo.packageName

            menu.findItem(R.id.ac_open)?.title = this.getLocalizedUIMessage(UIMessage(
                    R.string.web_view_open_in_chrome, arrayOf(resolveInfo.loadLabel(packageManager).toString())
            ))
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.ac_open -> {
                if(!mUrl.isNullOrEmpty()) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))
                    startActivity(browserIntent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  intent: Intent?) {
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (mUploadMessage == null) return
            mUploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
            mUploadMessage = null
        }
    }
}
