package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction

/**
 * Created by gabriel on 3/17/18.
 */

@Dao
interface ResetDao {
    @Query("DELETE from raw_session")
    fun deleteAllSessions()
    @Query("DELETE from raw_identitykey")
    fun deleteAllIdentityKeys()
    @Query("DELETE from raw_signedprekey")
    fun deleteAllSignedPreKey()
    @Query("DELETE from raw_prekey")
    fun deleteAllPreKey()
    @Query("DELETE from account")
    fun deleteAllAccounts()

    /**
     * Apparently transactions don't compile unless you pass at least one argument -__-
     */
    @Transaction
    fun deleteAllData(lol: Int) {
        deleteAllPreKey()
        deleteAllSignedPreKey()
        deleteAllSessions()
        deleteAllIdentityKeys()
        deleteAllAccounts()
    }
}