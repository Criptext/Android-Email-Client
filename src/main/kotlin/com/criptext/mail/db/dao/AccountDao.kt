package com.criptext.mail.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.criptext.mail.db.models.Account

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

    @Query("""UPDATE account
            SET name=:name
            where recipientId=:recipientId""")
    fun updateProfileName(name: String, recipientId: String)

    @Query("""UPDATE account
            SET jwt=:jwt
            where recipientId=:recipientId""")
    fun updateJwt(recipientId: String, jwt: String)

}
