package com.criptext.mail.utils.file

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.criptext.mail.BuildConfig
import com.criptext.mail.scenes.webview.WebViewActivity
import com.criptext.mail.scenes.webview.ui.WebViewUIObserver
import org.json.JSONObject

class CriptextJavaScriptInterface(val mContext: Context, val filename: String, val observer: WebViewUIObserver) {

    @JavascriptInterface
    fun postMessage(jsonString: String) {
        val json = JSONObject(jsonString)
        when(json.getString("type")){
            "close" -> {
                observer.onJSInterfaceClose()
            }
            "copy" -> {
                val params = json.optJSONObject("params") ?: return
                observer.onCopyText(params.getString("text"))
            }
        }
    }
}
