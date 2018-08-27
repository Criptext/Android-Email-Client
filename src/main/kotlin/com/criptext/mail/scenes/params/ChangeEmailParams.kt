package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.change_email.ChangeEmailActivity

class ChangeEmailParams(val recoveryEmail: String, val isConfirmed: Boolean): SceneParams(){
    override val activityClass = ChangeEmailActivity::class.java
}