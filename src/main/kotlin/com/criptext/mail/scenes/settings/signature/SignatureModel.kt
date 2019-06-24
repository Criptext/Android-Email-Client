package com.criptext.mail.scenes.settings.signature

import com.criptext.mail.scenes.SceneModel

class SignatureModel(val recipientId: String, val domain: String): SceneModel {
    var signature: String = ""
}