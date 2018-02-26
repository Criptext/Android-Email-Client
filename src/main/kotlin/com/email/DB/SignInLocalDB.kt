package com.email.DB

import android.content.Context

/**
 * Created by sebas on 2/15/18.
 */


interface SignInLocalDB {
    fun login(): Boolean

    class Default(applicationContext: Context): SignInLocalDB {
        override fun login(): Boolean {
            TODO("LOGIN NOT IMPLEMENTED")
        }

    }

}
