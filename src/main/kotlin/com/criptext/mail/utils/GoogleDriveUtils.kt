package com.criptext.mail.utils

import com.google.api.services.drive.Drive

object GoogleDriveUtils {
    fun checkForDriveBackup(mDriveServiceHelper: Drive): Pair<Long, String>?{
        val folder = mDriveServiceHelper.files().list().setQ("name='Criptext App'").execute()
        return if(folder.files.isEmpty()){
            null
        } else {
            val file = mDriveServiceHelper.files().list()
                    .setQ("name contains 'Mailbox Backup' and ('${folder.files.first().id}' in parents) and trashed=false")
                    .setFields("*")
                    .execute()
            if(file.files.isEmpty())
                null
            else {
                val driveFile = file.files.first()
                Pair(driveFile.getSize() / (1024 * 1024), DateAndTimeUtils.getHoraVerdadera(driveFile.modifiedTime.value))
            }
        }
    }
}