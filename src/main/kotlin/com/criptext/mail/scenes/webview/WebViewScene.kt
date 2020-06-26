package com.criptext.mail.scenes.webview

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.webkit.*
import android.widget.*
import com.criptext.mail.R
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.webview.ui.WebViewUIObserver
import com.criptext.mail.utils.*
import com.criptext.mail.utils.file.CriptextJavaScriptInterface

interface WebViewScene{

    val observer: WebViewUIObserver?
    fun attachView(observer: WebViewUIObserver, activeAccount: ActiveAccount, model: WebViewSceneModel,
    chromeClient: WebChromeClient)
    fun loadUrl(url: String?)
    fun setTitle(title: UIMessage)
    fun showError(message: UIMessage)
    fun showWebError()

    class WebViewSceneView(private val view: View,
                          private val keyboard: KeyboardManager): WebViewScene {

        val webViewCriptext: WebView by lazy { view.findViewById<WebView>(R.id.webViewCriptext) }
        val progressBar: ProgressBar by lazy { view.findViewById<ProgressBar>(R.id.progressBar) }
        val errorLayout: LinearLayout by lazy { view.findViewById<LinearLayout>(R.id.errorLayout) }
        private val backButton: ImageView by lazy {
            view.findViewById<ImageView>(R.id.web_view_back_button) as ImageView
        }
        private val toolbarTitle: TextView by lazy {
            view.findViewById<TextView>(R.id.web_view_toolbar_title) as TextView
        }

        override var observer: WebViewUIObserver? = null

        override fun attachView(observer: WebViewUIObserver,
                                activeAccount: ActiveAccount, model: WebViewSceneModel,
                                chromeClient: WebChromeClient) {

            this.observer = observer
            setupWebView(model, chromeClient)
            setListeners()
        }

        override fun loadUrl(url: String?) {
            if(url != null)
                webViewCriptext.loadUrl(url)
        }

        override fun setTitle(title: UIMessage) {
            toolbarTitle.text = view.context.getLocalizedUIMessage(title).capitalize()
        }

        override fun showError(message: UIMessage) {
            Toast.makeText(view.context, view.context.getLocalizedUIMessage(message), Toast.LENGTH_SHORT).show()
        }

        override fun showWebError() {
            webViewCriptext.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
        }

        private fun setupWebView(model: WebViewSceneModel, chromeClient: WebChromeClient) {
            webViewCriptext.settings.javaScriptEnabled = true
            webViewCriptext.settings.userAgentString = WebViewSceneController.userAgent
            webViewCriptext.settings.domStorageEnabled = true
            webViewCriptext.webChromeClient = chromeClient
            webViewCriptext.webViewClient = client
            webViewCriptext.setDownloadListener(downloadListener)

            val javascriptInterface = CriptextJavaScriptInterface(view.context, model.fileName, observer!!)
            webViewCriptext.addJavascriptInterface(javascriptInterface, "criptoBridge")
        }

        private val downloadListener = { url: String, _: String, contentDisposition: String,
                                         _: String, _: Long ->

            if (WebViewUtils.hasSDPermissionsWeb(view.context)) {
                if (url.contains("blob:")) {
                    WebViewUtils.injectRetrieveBlobData(webViewCriptext, url)
                } else {
                    observer?.onBrowserDownload(url, contentDisposition)
                }
            }
        }

        private val client = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                observer?.onUrlChanged(url)
                return loadUrlOnView(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                    view: WebView, request: WebResourceRequest): Boolean {
                observer?.onUrlChanged(request.url.toString())
                return loadUrlOnView(request.url.toString())
            }

            override fun onPageFinished(view: WebView, url: String) {
                toolbarTitle.text = view.title.capitalize()
                observer?.onPageFinishedLoading(url)
                progressBar.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                observer?.onPageStartedLoading(url)
                progressBar.visibility = View.VISIBLE
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                observer?.onPageReceiveError()
            }

            private fun loadUrlOnView(url: String): Boolean {
                if (URLUtil.isNetworkUrl(url)) {
                    return false
                }
                return true
            }
        }

        private fun setListeners(){
            backButton.setOnClickListener {
                observer?.onBackButtonPressed()
            }
        }
    }

}
