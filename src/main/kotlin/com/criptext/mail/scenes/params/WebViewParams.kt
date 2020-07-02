package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.webview.WebViewActivity
import com.criptext.mail.utils.UIMessage

data class WebViewParams(val url: String, val title: UIMessage?): SceneParams() {
    override val activityClass = WebViewActivity::class.java
}