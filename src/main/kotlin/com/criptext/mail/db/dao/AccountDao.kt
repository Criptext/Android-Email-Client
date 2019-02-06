package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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

    @Query("SELECT * FROM account WHERE isActive=1")
    fun getLoggedInAccount() : Account?

    @Query("UPDATE account SET isActive=0")
    fun updateActiveInAccount()

    @Query("DELETE FROM account")
    fun nukeTable()

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

    @Query("""UPDATE account
            SET refreshToken=:token
            where recipientId=:recipientId""")
    fun updateRefreshToken(recipientId: String, token: String)

}
