package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.CRFile

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

    @Query("SELECT * FROM file where id=:id")
    fun getFileById(id : Long) : CRFile?

    @Query("""SELECT * FROM file
            WHERE file.emailId=:emailId""")
    fun getAttachmentsFromEmail(emailId: Long) : List<CRFile>

    @Delete
    fun deleteAll(files: List<CRFile>)

}
