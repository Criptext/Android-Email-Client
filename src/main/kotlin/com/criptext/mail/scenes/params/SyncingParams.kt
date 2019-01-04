package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.syncing.SyncingActivity
import com.criptext.mail.utils.DeviceUtils

open class SyncingParams(val email: String, val deviceId: Int, val randomId: String,
                         val deviceType: DeviceUtils.DeviceType, val authorizerName: String): SceneParams() {
    override val activityClass = SyncingActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SyncingParams::class.java
    }

}
