package com.criptext.mail.db

import com.criptext.mail.db.models.Account
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailUtils
import java.io.File

/**
 * Created by sebas on 2/15/18.
 */


interface SignInLocalDB {
    fun login(): Boolean
    fun accountExistsLocally(username: String): Boolean
    fun getAccount(recipientId: String, domain: String): Account?
    fun deleteDatabase(account: Account)
    fun deleteDatabase(user: String, domain: String)
    fun deleteDatabase(users: List<String>)
    fun deleteDatabase()
    fun deleteSystemLabels()

    class Default(private val db: AppDatabase, private val filesDir: File): SignInLocalDB {

        override fun getAccount(recipientId: String, domain: String): Account? {
            return db.accountDao().getAccount(recipientId, domain)
        }

        override fun accountExistsLocally(username: String): Boolean {
            val account = db.accountDao().getLoggedInAccount()
            if(account == null) return false
            else if(account.recipientId == username) return true
            return false
        }

        override fun login(): Boolean {
            TODO("LOGIN NOT IMPLEMENTED")
        }

        override fun deleteDatabase() {
            db.clearAllTables()
        }

        override fun deleteDatabase(user: String, domain: String) {
            EmailUtils.deleteEmailsInFileSystem(filesDir, user, domain)
            db.accountDao().deleteAccountByRecipientId(user, domain)
        }

        override fun deleteDatabase(users: List<String>) {
            users.forEach {
                val domain = EmailAddressUtils.extractEmailAddressDomain(it)
                val recipientId = EmailAddressUtils.extractRecipientIdFromAddress(it, domain)
                deleteDatabase(recipientId, domain)
            }
        }

        override fun deleteDatabase(account: Account) {
            EmailUtils.deleteEmailsInFileSystem(filesDir, account.recipientId, account.domain)
            db.accountDao().delete(account)
        }

        override fun deleteSystemLabels() {
            db.labelDao().nukeTable()
        }

    }

}
