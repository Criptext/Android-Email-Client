package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.syncing.SyncingActivity
import com.criptext.mail.utils.DeviceUtils

open class SyncingParams(): SceneParams() {
    override val activityClass = SyncingActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == SyncingParams::class.java
    }

}
