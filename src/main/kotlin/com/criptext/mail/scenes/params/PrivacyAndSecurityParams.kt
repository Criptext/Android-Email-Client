package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.privacyandsecurity.PrivacyAndSecurityActivity

class PrivacyAndSecurityParams(val hasReadReceipts: Boolean): SceneParams(){
    override val activityClass = PrivacyAndSecurityActivity::class.java
}