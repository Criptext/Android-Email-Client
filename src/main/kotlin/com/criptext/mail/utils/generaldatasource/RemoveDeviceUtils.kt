package com.criptext.mail.utils.generaldatasource

import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage

object RemoveDeviceUtils{
    fun clearAllData(db: AppDatabase, storage: KeyValueStorage){
        db.clearAllTables()
        storage.clearAll()
    }
}