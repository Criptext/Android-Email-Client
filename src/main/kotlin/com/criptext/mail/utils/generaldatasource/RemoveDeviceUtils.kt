package com.criptext.mail.utils.generaldatasource

import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.services.jobs.CloudBackupJobService

object RemoveDeviceUtils{
    fun clearAllData(db: AppDatabase, storage: KeyValueStorage, recipientId: String, accountId: Long){
        db.accountDao().deleteAccountByRecipientId(recipientId)
        CloudBackupJobService.cancelJob(storage, accountId)
    }
}