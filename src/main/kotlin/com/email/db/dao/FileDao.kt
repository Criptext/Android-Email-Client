package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.File

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface FileDao {

    @Insert
    fun insert(file : File)

    @Insert
    fun insertAll(files : List<File>)

    @Query("SELECT * FROM file")
    fun getAll() : List<File>


    @Query("""SELECT * FROM file
            WHERE file.emailId=:emailId""")
    fun getAttachmentsFromEmail(emailId: Int) : List<File>

    @Delete
    fun deleteAll(files: List<File>)

}
