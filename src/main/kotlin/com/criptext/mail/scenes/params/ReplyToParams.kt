package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.profile.data.ProfileUserData
import com.criptext.mail.scenes.settings.replyto.ReplyToActivity

class ReplyToParams(val userData: ProfileUserData): SceneParams(){
    override val activityClass = ReplyToActivity::class.java
}