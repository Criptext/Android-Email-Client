package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.FileKey

@Dao
interface FileKeyDao {

    @Insert
    fun insert(file : FileKey)

    @Insert
    fun insertAll(files : List<FileKey>)

    @Query("SELECT * FROM file_key")
    fun getAll() : List<FileKey>

    @Query("SELECT * FROM file_key where id=:id")
    fun getFileById(id : Long) : FileKey?

    @Query("""SELECT * FROM file_key
            WHERE file_key.emailId=:emailId""")
    fun getAttachmentKeyFromEmail(emailId: Long) : FileKey

    @Delete
    fun deleteAll(files: List<FileKey>)

}