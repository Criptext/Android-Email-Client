package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.criptext.mail.db.models.FileKey

@Dao
interface FileKeyDao {

    @Insert
    fun insert(file : FileKey)

    @Insert
    fun insertAll(files : List<FileKey>)

    @Query("SELECT * FROM file_key")
    fun getAll() : List<FileKey>

    @Query("""SELECT * FROM file_key
        LEFT JOIN email on email.id = file_key.emailId
        WHERE delivered NOT IN (1,4)
        AND file_key.emailId NOT IN
        (SELECT email_label.emailId FROM email_label WHERE email_label.emailId = file_key.emailId and email_label.labelId=6)
    """)
    fun getAllForLinkFile() : List<FileKey>

    @Query("SELECT * FROM file_key where id=:id")
    fun getFileById(id : Long) : FileKey?

    @Query("""SELECT * FROM file_key
            WHERE file_key.emailId=:emailId""")
    fun getAttachmentKeyFromEmail(emailId: Long) : FileKey?

    @Delete
    fun deleteAll(files: List<FileKey>)

}