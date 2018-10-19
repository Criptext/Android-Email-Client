package com.criptext.mail.scenes.emaildetail

import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.otaliastudios.zoom.ZoomLayout


/**
 * Created by hirobreak on 19/07/17.
 */
class WebviewJavascriptInterface(val mContext : Context, val zoomLayout : ZoomLayout, val webView: WebView) {

    @JavascriptInterface
    fun toggleButton() {
        (mContext as Activity).runOnUiThread {
            webView.postDelayed({
                zoomLayout.realZoomTo(1.0f, false)
            }, 500)
        }
    }
}