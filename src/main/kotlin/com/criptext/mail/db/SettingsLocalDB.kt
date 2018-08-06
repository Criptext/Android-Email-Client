package com.criptext.mail.db

import com.criptext.mail.db.dao.*
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.EmailContact
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.utils.DateUtils
import com.criptext.mail.utils.HTMLUtils
import java.util.*

/**
 * Created by danieltigse on 6/14/18.
 */

interface SettingsLocalDB{
    val labelDao:LabelDao
    val accountDao:AccountDao
    val contactDao:ContactDao
    fun logoutNukeDB()
    class Default(private val db: AppDatabase): SettingsLocalDB {
        override val labelDao: LabelDao = db.labelDao()
        override val accountDao: AccountDao = db.accountDao()
        override val contactDao: ContactDao = db.contactDao()
        override fun logoutNukeDB() {
            db.clearAllTables()
        }
    }
}

