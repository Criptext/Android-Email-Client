package com.criptext.mail.db.dao

import android.arch.persistence.room.*
import com.criptext.mail.db.models.*

@Dao
interface EmailExternalSessionDao {
    @Insert
    fun insert(externalSession : EmailExternalSession)

    @Delete
    fun delete(externalSession: EmailExternalSession)

    @Query("""SELECT * FROM email_external_session
        WHERE emailId=:emailId""")
    fun getExternalSessionByEmailId(emailId: Long): EmailExternalSession?

}