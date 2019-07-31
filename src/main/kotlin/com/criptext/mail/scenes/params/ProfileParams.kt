package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.profile.ProfileActivity
import com.criptext.mail.scenes.settings.profile.data.ProfileUserData

class ProfileParams(val comesFromMailbox: Boolean): SceneParams(){
    override val activityClass = ProfileActivity::class.java
}