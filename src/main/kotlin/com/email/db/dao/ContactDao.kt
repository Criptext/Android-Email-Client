package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.models.Contact

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnoringConflicts(contact : Contact): Long

    @Insert
    fun insertAll(users : List<Contact>)

    @Query("SELECT * FROM contact")
    fun getAll() : List<Contact>

    @Query("SELECT * FROM contact where email=:email")
    fun getContact(email : String) : Contact?

    @Query("SELECT * FROM contact where id=:id")
    fun getContactById(id : Long) : Contact?

    @Delete
    fun deleteAll(contacts: List<Contact>)

    @Query("""UPDATE contact
            SET name=:name
            where email=:email""")
    fun updateContactName(email: String, name: String)
}
