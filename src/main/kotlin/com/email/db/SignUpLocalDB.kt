package com.email.db

import android.content.Context
import com.email.db.models.Account
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSignedPreKey

/**
 * Created by sebas on 2/16/18.
 */

interface SignUpLocalDB {
    fun storePrekeys(prekeys: Map<Int, String>)
    fun deletePrekeys()
    fun storeRawSignedPrekey(crSignedPreKey: CRSignedPreKey)
    fun saveAccount(account: Account)

    class Default(applicationContext: Context): SignUpLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun storePrekeys(prekeys: Map<Int, String>) {
            val listPrekeys: ArrayList<CRPreKey> = ArrayList()

            for ((key, value) in prekeys) {
                listPrekeys.add(CRPreKey(id = key, byteString = value))
            }

            db.rawPreKeyDao().insertAll(listPrekeys)
        }

        override fun saveAccount(account: Account) {
            db.accountDao().insert(account)
        }

        override fun storeRawSignedPrekey(crSignedPreKey: CRSignedPreKey) {
            db.rawSignedPreKeyDao().insert(crSignedPreKey)
        }

        override fun deletePrekeys() {
            db.rawPreKeyDao().deleteAll()
        }
    }
}
