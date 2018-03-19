package com.email.db

import android.content.Context
import android.util.Log
import com.email.db.models.Account
import com.email.db.models.signal.CRPreKey
import com.email.db.models.signal.CRSignedPreKey
import com.email.signal.SignalKeyGenerator

/**
 * Created by sebas on 2/16/18.
 */

interface SignUpLocalDB {
    fun saveNewUserData(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle)

    class Default(private val db: AppDatabase): SignUpLocalDB {

        constructor(ctx: Context): this (AppDatabase.getAppDatabase(ctx))

        private fun storePreKeys(preKeys: Map<Int, String>) {
            val listPreKeys: ArrayList<CRPreKey> = ArrayList()

            for ((key, value) in preKeys) {
                listPreKeys.add(CRPreKey(id = key, byteString = value))
            }

            db.rawPreKeyDao().insertAll(listPreKeys)
        }

        private fun saveAccount(account: Account) {
            db.accountDao().insert(account)
        }

        private fun storeRawSignedPreKey(crSignedPreKey: CRSignedPreKey) {
            db.rawSignedPreKeyDao().insert(crSignedPreKey)
        }

        private fun deletePreKeys() {
            db.rawPreKeyDao().deleteAll()
        }

        override fun saveNewUserData(account: Account, keyBundle: SignalKeyGenerator.PrivateBundle) {
            saveAccount(account)
            deletePreKeys()
            storePreKeys(keyBundle.preKeys)
            storeRawSignedPreKey(CRSignedPreKey(
                    keyBundle.signedPreKeyId,
                    keyBundle.signedPreKey))
        }

    }
}
