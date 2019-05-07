package com.criptext.mail.scenes.settings.cloudbackup.data

import java.util.*

data class CloudBackupData(val hasCloudBackup: Boolean, val autoBackupFrequency: Int,
                           val useWifiOnly: Boolean, val lastModified: Date?, val fileSize: Long)