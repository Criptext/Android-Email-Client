package com.email.db

import android.content.Context

/**
 * Created by sebas on 2/15/18.
 */


interface SignInLocalDB {
    fun login(): Boolean
    fun userExistsLocally(username: String): Boolean

    class Default(applicationContext: Context): SignInLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun userExistsLocally(username: String): Boolean {
            val user = db.userDao().getLoggedInUser()
            if(user == null) return false
            else if(user.nickname == username) return true
            return false
        }

        override fun login(): Boolean {
            TODO("LOGIN NOT IMPLEMENTED")
        }

    }

}
