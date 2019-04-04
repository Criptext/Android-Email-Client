package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.settings.cloudbackup.CloudBackupActivity

open class CloudBackupParams: SceneParams() {
    override val activityClass = CloudBackupActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == CloudBackupParams::class.java
    }

}
