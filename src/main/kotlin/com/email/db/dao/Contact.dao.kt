package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.Contact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface ContactDao {

    @Insert
    fun insert(contact : Contact)

    @Insert
    fun insertAll(users : List<Contact>)

    @Query("SELECT * FROM contact")
    fun getAll() : List<Contact>

    @Query("SELECT * FROM contact LIMIT 1")
    fun getLoggedInUser() : Contact?

    @Delete
    fun deleteAll(users: List<Contact>)

}
