package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import com.criptext.mail.db.models.EmailLabel

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
    @Query("DELETE from label")
    fun deleteAllLabels()
    @Query("DELETE from email")
    fun deleteAllEmails()
    @Query("DELETE from contact")
    fun deleteAllContacts()
    @Query("DELETE from email_external_session")
    fun deleteAllExternalSessions()

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
        deleteAllEmails()
        deleteAllLabels()
        deleteAllContacts()
        deleteAllExternalSessions()
    }
}