package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
        WHERE id > :lastId
        AND EXISTS
        (SELECT * FROM email WHERE delivered NOT IN (1, 4)
        AND email.id = file.emailId AND email.accountId = :accountId)
        AND NOT EXISTS
        (SELECT * FROM email_label WHERE email_label.emailId = file.emailId and email_label.labelId=6)
        ORDER BY id
        LIMIT :limit
    """)
    fun getAllForLinkFile(limit: Int, lastId: Long, accountId:Long) : List<CRFile>

    @Query("SELECT * FROM file where id=:id")
    fun getFileById(id : Long) : CRFile?

    @Query("UPDATE file SET token=:newToken where id=:id")
    fun updateToken(id : Long, newToken: String)

    @Query("""SELECT * FROM file
            WHERE file.emailId=:emailId""")
    fun getAttachmentsFromEmail(emailId: Long) : List<CRFile>

    @Query("""SELECT * FROM file
            WHERE file.emailId in (:emailId)""")
    fun getAttachmentsFromEmails(emailId: List<Long>) : List<CRFile>

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

    @Query("DELETE FROM file")
    fun nukeTable()

}
