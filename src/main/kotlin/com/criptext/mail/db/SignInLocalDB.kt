package com.criptext.mail.db

import android.content.Context
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EmailUtils
import java.io.File

/**
 * Created by sebas on 2/15/18.
 */


interface SignInLocalDB {
    fun login(): Boolean
    fun accountExistsLocally(username: String): Boolean
    fun deleteDatabase(account: Account)
    fun deleteDatabase(user: String, domain: String)
    fun deleteDatabase(users: List<String>)
    fun deleteDatabase()

    class Default(applicationContext: Context, private val filesDir: File): SignInLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

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
            val username = if(domain == Contact.mainDomain) user
            else user.plus("@$domain")
            EmailUtils.deleteEmailsInFileSystem(filesDir, username)
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
            val username = if(account.domain == Contact.mainDomain) account.recipientId
            else account.recipientId.plus("@${account.domain}")
            EmailUtils.deleteEmailsInFileSystem(filesDir, username)
            db.accountDao().delete(account)
        }

    }

}
