package com.criptext.mail.scenes.restorebackup

import com.criptext.mail.scenes.restorebackup.holders.RestoreBackupLayoutState
import com.google.api.services.drive.Drive

class RestoreBackupModel(val isLocal: Boolean, val localFile: Pair<String, Boolean>?) {
    var state: RestoreBackupLayoutState = RestoreBackupLayoutState.Searching()
    var accountEmail = ""
    var mDriveServiceHelper: Drive? = null
    var backupSize = 0L
    var lastModified = 0L
    var backupFilePath = ""
    var isFileEncrypted = false
    var hasPathReady = false
    var passphrase: String? = null
}