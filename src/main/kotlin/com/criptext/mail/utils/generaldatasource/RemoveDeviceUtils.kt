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
        val username = if(activeAccount.domain == Contact.mainDomain) activeAccount.recipientId
        else activeAccount.userEmail
        EmailUtils.deleteEmailsInFileSystem(filesDir, username)
        db.accountDao().deleteAccountById(activeAccount.id)
        CloudBackupJobService.cancelJob(storage, activeAccount.id)
    }
}