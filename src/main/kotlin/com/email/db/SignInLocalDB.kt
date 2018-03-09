package com.email.db

import android.content.Context

/**
 * Created by sebas on 2/15/18.
 */


interface SignInLocalDB {
    fun login(): Boolean
    fun accountExistsLocally(username: String): Boolean

    class Default(applicationContext: Context): SignInLocalDB {

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

    }

}
