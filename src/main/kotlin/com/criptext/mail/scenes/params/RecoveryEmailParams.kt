package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.profile.data.ProfileUserData
import com.criptext.mail.scenes.settings.recovery_email.RecoveryEmailActivity

class RecoveryEmailParams(val userData: ProfileUserData): SceneParams(){
    override val activityClass = RecoveryEmailActivity::class.java
}