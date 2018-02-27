package com.email.db.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.User

/**
 * Created by sebas on 2/7/18.
 */

@Dao
interface UserDao {

    @Insert
    fun insert(user : User)

    @Insert
    fun insertAll(users : List<User>)

    @Query("SELECT * FROM user")
    fun getAll() : List<User>

    @Delete
    fun deleteAll(users: List<User>)

}
