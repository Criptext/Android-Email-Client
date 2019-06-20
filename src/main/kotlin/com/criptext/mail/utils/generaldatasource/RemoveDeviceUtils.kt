package com.criptext.mail.utils.generaldatasource

import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.services.jobs.CloudBackupJobService
import com.criptext.mail.utils.EmailUtils
import java.io.File

object RemoveDeviceUtils{
    fun clearAllData(db: AppDatabase, storage: KeyValueStorage, activeAccount: ActiveAccount, filesDir: File){
        EmailUtils.deleteEmailsInFileSystem(filesDir, activeAccount.recipientId, activeAccount.domain)
        db.accountDao().deleteAccountById(activeAccount.id)
        CloudBackupJobService.cancelJob(storage, activeAccount.id)
    }
}