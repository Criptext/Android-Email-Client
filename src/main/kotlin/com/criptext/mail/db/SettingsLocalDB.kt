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
    fun logoutNukeDB()
    fun logout()
    class Default(private val db: AppDatabase): SettingsLocalDB {

        private val account by lazy { db.accountDao().getLoggedInAccount()!! }

        override val labelDao: LabelDao = db.labelDao()
        override val accountDao: AccountDao = db.accountDao()
        override val contactDao: ContactDao = db.contactDao()
        override val pendingEventDao: PendingEventDao = db.pendingEventDao()
        override fun logoutNukeDB() {
            db.clearAllTables()
        }
        override fun logout() {
            db.rawIdentityKeyDao().deleteAll(account.id)
            db.rawPreKeyDao().deleteAll(account.id)
            db.rawSessionDao().deleteAll(account.id)
            db.rawSignedPreKeyDao().deleteAll(account.id)
        }
    }
}

