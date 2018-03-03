package com.email.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.db.models.Account

/**
 * Created by sebas on 3/3/18.
 */


@Dao
interface AccountDao {

    @Insert
    fun insertAll(accounts : List<Account>)

    @Insert
    fun insert(account :Account)

    @Query("SELECT * FROM account")
    fun getAll() : List<Account>

    @Query("SELECT * FROM account LIMIT 1")
    fun getLoggedInAccount() : Account?

    @Delete
    fun deleteAll(accounts: List<Account>)

}
