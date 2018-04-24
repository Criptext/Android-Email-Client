package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Transaction
import com.email.db.models.Email

/**
 * Created by gabriel on 4/23/18.
 */
@Dao
interface MailboxDao {

    @Insert
    fun insertEmail(email: Email): Int

    @Transaction
    fun insertNewReceivedEmail(email: Email) {
        val newEmailId = insertEmail(email)
        // TODO set label

    }

}
