package com.email.scenes.params

import com.email.SignUpActivity

/**
 * Created by sebas on 2/23/18.
 */

class SignUpParams: SceneParams() {
    override val activityClass = SignUpActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SignUpParams::class.java
    }
}
