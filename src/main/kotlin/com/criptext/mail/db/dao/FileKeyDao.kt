package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.FileKey

@Dao
interface FileKeyDao {

    @Insert
    fun insert(file : FileKey)

    @Insert
    fun insertAll(files : List<FileKey>)

    @Query("UPDATE file SET fileKey = :fileKey  WHERE file.emailId = :emailId")
    fun insertFileKeyForFiles(fileKey: String, emailId: Long)

    @Query("SELECT * FROM file_key")
    fun getAll() : List<FileKey>

    @Query("""SELECT * FROM file_key
        WHERE id > :lastId
        AND EXISTS
        (SELECT * FROM email WHERE delivered NOT IN (1, 4)
        AND email.id = file_key.emailId)
        AND NOT EXISTS
        (SELECT * FROM email_label WHERE email_label.emailId = file_key.emailId
        AND email_label.labelId=6)
        ORDER BY id
        LIMIT :limit
    """)
    fun getAllForLinkFile(limit: Int, lastId: Long) : List<FileKey>

    @Query("SELECT * FROM file_key where id=:id")
    fun getFileById(id : Long) : FileKey?

    @Query("""SELECT * FROM file_key
            WHERE file_key.emailId=:emailId""")
    fun getAttachmentKeyFromEmail(emailId: Long) : FileKey?

    @Query("""SELECT * FROM file_key
            WHERE file_key.emailId in (:emailIds)""")
    fun getAttachmentKeyFromEmails(emailIds: List<Long>) : List<FileKey>

    @Delete
    fun deleteAll(files: List<FileKey>)

    @Query("DELETE FROM file_key")
    fun nukeTable()

}