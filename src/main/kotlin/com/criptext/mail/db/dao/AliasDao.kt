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

    @Query("SELECT * FROM alias WHERE name = :aliasName AND domain=:domain AND accountId=:accountId")
    fun getAliasByName(aliasName: String, domain: String?, accountId: Long) : Alias?

    @Query("SELECT * FROM alias WHERE name = :aliasName AND domain=:domain")
    fun getAliasByName(aliasName: String, domain: String?) : Alias?

    @Query("SELECT * FROM alias WHERE name = :aliasName AND domain IS NULL AND accountId=:accountId")
    fun getCriptextAliasByName(aliasName: String, accountId: Long) : Alias?

    @Query("SELECT * FROM alias WHERE name = :aliasName AND domain IS NULL")
    fun getCriptextAliasByName(aliasName: String) : Alias?

    @Query("SELECT * FROM alias")
    fun getAll() : List<Alias>

    @Query("SELECT * FROM alias WHERE accountId=:accountId")
    fun getAll(accountId: Long) : List<Alias>

    @Query("UPDATE alias SET active=:enable WHERE name = :aliasName AND domain=:domain AND accountId=:accountId")
    fun updateActive(aliasName: String, domain: String?, enable: Boolean, accountId: Long)

    @Query("UPDATE alias SET active=:enable WHERE name = :aliasName AND domain IS NULL AND accountId=:accountId")
    fun updateCriptextActive(aliasName: String, enable: Boolean, accountId: Long)

    @Query("DELETE FROM alias where domain=:domain")
    fun deleteByDomain(domain: String)

    @Query("DELETE FROM alias WHERE name = :aliasName AND domain=:domain AND accountId=:accountId")
    fun deleteByName(aliasName: String, domain: String?, accountId: Long)

    @Query("DELETE FROM alias")
    fun nukeTable()

    @Delete
    fun delete(address: Alias)

    @Delete
    fun deleteAll(addresses: List<Alias>)
}
