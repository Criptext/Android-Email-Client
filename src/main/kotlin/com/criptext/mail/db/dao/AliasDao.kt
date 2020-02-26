package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.Alias
import com.criptext.mail.db.models.CustomDomain

@Dao
interface AliasDao {

    @Insert
    fun insertAll(aliases : List<Alias>)

    @Insert
    fun insert(alias :Alias)

    @Query("SELECT * FROM alias")
    fun getAll() : List<Alias>

    @Query("SELECT * FROM alias WHERE accountId=:accountId")
    fun getAliasByAccountId(accountId: Long) : Alias?

    @Query("DELETE FROM alias where domain=:domain")
    fun deleteByDomain(domain: String)

    @Query("DELETE FROM alias")
    fun nukeTable()

    @Delete
    fun delete(address: CustomDomain)

    @Delete
    fun deleteAll(addresses: List<CustomDomain>)
}
