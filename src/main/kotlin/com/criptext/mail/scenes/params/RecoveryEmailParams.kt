package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.recovery_email.RecoveryEmailActivity

class RecoveryEmailParams(val isConfirmed: Boolean, val recoveryEmail: String): SceneParams(){
    override val activityClass = RecoveryEmailActivity::class.java
}