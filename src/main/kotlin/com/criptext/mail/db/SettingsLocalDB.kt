package com.criptext.mail.db

import com.criptext.mail.db.dao.*

/**
 * Created by danieltigse on 6/14/18.
 */

interface SettingsLocalDB{
    val labelDao:LabelDao
    val accountDao:AccountDao
    val contactDao:ContactDao
    val pendingEventDao:PendingEventDao
    val customDomainDao: CustomDomainDao
    fun logoutNukeDB()
    fun logout(accountId: Long)

    class Default(private val db: AppDatabase): SettingsLocalDB {

        override val labelDao: LabelDao = db.labelDao()
        override val accountDao: AccountDao = db.accountDao()
        override val contactDao: ContactDao = db.contactDao()
        override val pendingEventDao: PendingEventDao = db.pendingEventDao()
        override val customDomainDao: CustomDomainDao = db.customDomainDao()
        override fun logoutNukeDB() {
            db.clearAllTables()
        }
        override fun logout(accountId: Long) {
            db.rawIdentityKeyDao().deleteAll(accountId)
            db.rawPreKeyDao().deleteAll(accountId)
            db.rawSessionDao().deleteAll(accountId)
            db.rawSignedPreKeyDao().deleteAll(accountId)
        }
    }
}

