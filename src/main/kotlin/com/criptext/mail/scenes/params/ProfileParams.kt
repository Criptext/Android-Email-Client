package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.profile.ProfileActivity

class ProfileParams(val name: String): SceneParams(){
    override val activityClass = ProfileActivity::class.java
}