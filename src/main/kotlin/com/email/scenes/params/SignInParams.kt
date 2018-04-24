package com.email.scenes.params

import com.email.scenes.signin.SignInActivity

/**
 * Created by gabriel on 4/19/18.
 */
open class SignInParams: SceneParams() {
    override val activityClass = SignInActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SignInParams::class.java
    }

}
