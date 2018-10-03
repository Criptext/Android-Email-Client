package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.CRFile

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface FileDao {

    @Insert
    fun insert(file : CRFile)

    @Insert
    fun insertAll(files : List<CRFile>)

    @Query("SELECT * FROM file")
    fun getAll() : List<CRFile>

    @Query("""SELECT * FROM file
        LEFT JOIN email on file.emailId=email.id
        WHERE delivered NOT IN (1, 4)
        AND file.emailId NOT IN
        (SELECT email_label.emailId FROM email_label WHERE email_label.emailId = file.emailId and email_label.labelId=6)
    """)
    fun getAllForLinkFile() : List<CRFile>

    @Query("SELECT * FROM file where id=:id")
    fun getFileById(id : Long) : CRFile?

    @Query("""SELECT * FROM file
            WHERE file.emailId=:emailId""")
    fun getAttachmentsFromEmail(emailId: Long) : List<CRFile>

    @Query("""UPDATE file
            SET status=:status
            WHERE file.emailId=:emailId""")
    fun changeFileStatusByEmailid(emailId: Long, status: Int)

    @Query("""UPDATE file
            SET status=:status
            WHERE file.emailId in (:emailIds)""")
    fun changeFileStatusByEmailIds(emailIds: List<Long>, status: Int)

    @Delete
    fun deleteAll(files: List<CRFile>)

}
