package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.replyto.ReplyToActivity

class ReplyToParams(val replyToEmail: String): SceneParams(){
    override val activityClass = ReplyToActivity::class.java
}