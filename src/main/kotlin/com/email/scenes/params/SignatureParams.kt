package com.email.scenes.params

import com.email.scenes.settings.signature.SignatureActivity

class SignatureParams(val recipientId: String): SceneParams(){
    override val activityClass = SignatureActivity::class.java
}