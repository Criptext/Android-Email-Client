package com.criptext.mail.scenes.emaildetail

import android.app.Activity
import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.LinearLayout
import com.criptext.mail.scenes.emaildetail.ui.EmailDetailUIObserver
import com.criptext.mail.scenes.emaildetail.ui.FullEmailListAdapter
import com.otaliastudios.zoom.ZoomLayout


/**
 * Created by hirobreak on 19/07/17.
 */
class WebviewJavascriptInterface(val mContext : Context, val zoomLayout : ZoomLayout,
                                 val webView: WebView, val listener: FullEmailListAdapter.OnFullEmailEventListener? = null) {

    @JavascriptInterface
    fun toggleButton() {
        (mContext as Activity).runOnUiThread {
            webView.postDelayed({
                zoomLayout.realZoomTo(1.0f, false)
            }, 500)
        }
    }
}