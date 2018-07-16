package com.email.scenes

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.email.R
import com.email.utils.DownloadHelper
import com.email.utils.file.DownloadBlobInterface
import com.email.utils.WebViewUtils

class WebViewActivity : AppCompatActivity() {
    val userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/LMY48B) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36"

    val webViewCriptext: WebView by lazy { findViewById<WebView>(R.id.webViewCriptext) }
    var mUrl: String? = null

    private val client = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            mUrl = url
            view.loadUrl(url)
            return true
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest): Boolean {
            mUrl = request.url.toString()
            view.loadUrl(request.url.toString())
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            supportActionBar?.title = view.title
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

        setupWebView()
        setupActionBar()
        tryToRestoreState(savedInstanceState)
    }

    private fun setupWebView() {
        webViewCriptext.webChromeClient = WebChromeClient()
        webViewCriptext.settings.javaScriptEnabled = true
        webViewCriptext.settings.userAgentString = userAgent
        webViewCriptext.webViewClient = client
        webViewCriptext.setDownloadListener(downloadListener)

        val javascriptInterface = DownloadBlobInterface(this, intent.getStringExtra("name") ?: "noname")
        webViewCriptext.addJavascriptInterface(javascriptInterface, "CriptextSecureEmail")
    }

    private fun setupActionBar() {
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.exit)
    }

    private fun tryToRestoreState(savedInstanceState: Bundle?) {
        if (intent.getStringExtra("url") != null && savedInstanceState == null) {
            mUrl = intent.getStringExtra("url")
            webViewCriptext.loadUrl(mUrl)
        } else if (savedInstanceState != null) {
            webViewCriptext.restoreState(savedInstanceState)
        }
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_webview, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.ac_open -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mUrl))
                startActivity(browserIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_out_down)
    }

}
