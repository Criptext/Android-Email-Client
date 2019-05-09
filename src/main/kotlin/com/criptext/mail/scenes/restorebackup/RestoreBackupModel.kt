package com.criptext.mail.scenes.restorebackup

import com.google.api.services.drive.Drive

class RestoreBackupModel() {
    var accountEmail = ""
    var mDriveServiceHelper: Drive? = null
    var backupSize = 0L
    var lastModified = 0L
    var backupFilePath = ""
    var isFileEncrypted = false
    var hasPathReady = false
    var passphrase: String? = null
}