package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.linking.LinkingActivity
import com.criptext.mail.utils.DeviceUtils

open class LinkingParams(val email: String, val deviceId: Int, val randomId: String,
                         val deviceType: DeviceUtils.DeviceType): SceneParams() {
    override val activityClass = LinkingActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other is LinkingParams
    }
}
