package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.signup.customize.CustomizeActivity


class CustomizeParams(val recoveryEmail: String): SceneParams() {
    override val activityClass = CustomizeActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == CustomizeParams::class.java
    }
}
