package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.signup.SignUpActivity

/**
 * Created by sebas on 2/23/18.
 */

class SignUpParams(val isMultiple: Boolean = false): SceneParams() {
    override val activityClass = SignUpActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SignUpParams::class.java
    }
}
