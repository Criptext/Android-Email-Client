package com.email.db

import android.content.Context

/**
 * Created by sebas on 3/12/18.
 */

interface EmailDetailLocalDB {

    class Default(applicationContext: Context): EmailDetailLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)
    }
}
