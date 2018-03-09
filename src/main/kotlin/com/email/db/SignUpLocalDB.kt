package com.email.db

import android.content.Context
import com.email.db.models.User
import com.email.db.models.signal.RawPreKey
import com.email.db.models.signal.RawSignedPreKey

/**
 * Created by sebas on 2/16/18.
 */

interface SignUpLocalDB {
    fun storePrekeys(prekeys: Map<Int, String>)
    fun deletePrekeys()
    fun saveUser(user: User)
    fun storeRawSignedPrekey(rawSignedPreKey: RawSignedPreKey)

    class Default(applicationContext: Context): SignUpLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun storePrekeys(prekeys: Map<Int, String>) {
            val listPrekeys: ArrayList<RawPreKey> = ArrayList()

            for ((key, value) in prekeys) {
                listPrekeys.add(RawPreKey(id = key, byteString = value))
            }

            db.rawPreKeyDao().insertAll(listPrekeys)
        }

        override fun saveUser(user: User) {
            db.userDao().insert(user)
        }


        override fun storeRawSignedPrekey(rawSignedPreKey: RawSignedPreKey) {
            db.rawSignedPreKeyDao().insert(rawSignedPreKey)
        }

        override fun deletePrekeys() {
            db.rawPreKeyDao().deleteAll()
        }
    }
}
