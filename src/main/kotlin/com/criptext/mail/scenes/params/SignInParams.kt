package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.signin.SignInActivity

/**
 * Created by gabriel on 4/19/18.
 */
open class SignInParams(val isMultiple: Boolean = false): SceneParams() {
    override val activityClass = SignInActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SignInParams::class.java
    }

}
