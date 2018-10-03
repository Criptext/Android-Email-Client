package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.linking.LinkingActivity

open class LinkingParams(val email: String, val deviceId: Int, val randomId: String): SceneParams() {
    override val activityClass = LinkingActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other is LinkingParams
    }
}
