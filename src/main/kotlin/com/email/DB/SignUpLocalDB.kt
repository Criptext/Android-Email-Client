package com.email.DB

import android.content.Context

/**
 * Created by sebas on 2/16/18.
 */

interface SignUpLocalDB {
    fun login(): Boolean

    class Default(applicationContext: Context): SignUpLocalDB {
        override fun login(): Boolean {
            TODO("LOGIN NOT IMPLEMENTED")
        }

    }

}
