package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.webview.WebViewActivity

data class WebViewParams(val url: String): SceneParams() {
    override val activityClass = WebViewActivity::class.java
}