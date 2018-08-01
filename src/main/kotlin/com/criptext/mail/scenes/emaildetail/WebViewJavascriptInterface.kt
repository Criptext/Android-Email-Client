package com.criptext.mail.scenes.emaildetail

import android.app.Activity
import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.LinearLayout
import com.criptext.mail.utils.ui.ZoomLayout

/**
 * Created by hirobreak on 19/07/17.
 */
class WebviewJavascriptInterface(
        val mContext : Context,
        val zoomLayout : ZoomLayout) {

    @JavascriptInterface
    fun toggleButton() {
        (mContext as Activity).runOnUiThread {
            zoomLayout.scale = 1.0f
            zoomLayout.applyScaleAndTranslation()
            zoomLayout.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            zoomLayout.requestLayout()
        }
    }
}
