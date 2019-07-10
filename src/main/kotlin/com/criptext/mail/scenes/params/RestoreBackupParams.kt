package com.criptext.mail.scenes.params

import com.criptext.mail.scenes.restorebackup.RestoreBackupActivity

open class RestoreBackupParams(val isLocal: Boolean = false, val localFile: Pair<String, Boolean>? = null): SceneParams() {
    override val activityClass = RestoreBackupActivity::class.java

    override fun equals(other: Any?): Boolean {
        return other is RestoreBackupParams
    }
}
