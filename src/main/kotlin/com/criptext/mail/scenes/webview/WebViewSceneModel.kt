package com.criptext.mail.scenes.webview

import android.net.Uri
import android.webkit.ValueCallback
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.UIMessage

class WebViewSceneModel(val title: UIMessage?, var mUrl: String? = null, val browserName: String? = null): SceneModel {
    var mUploadMessage: ValueCallback<Array<Uri>>? = null

    var isOnAdmin = false
    var comesFromMailbox = false
    var initialAccountType: AccountTypes? = null

    var fileName = "noname"
}
