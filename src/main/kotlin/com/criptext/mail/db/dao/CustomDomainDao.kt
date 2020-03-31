package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.criptext.mail.db.models.CustomDomain

@Dao
interface CustomDomainDao {

    @Insert
    fun insertAll(customDomains : List<CustomDomain>)

    @Insert
    fun insert(customDomain :CustomDomain)

    @Query("SELECT * FROM customDomain")
    fun getAll() : List<CustomDomain>

    @Query("SELECT * FROM customDomain WHERE accountId=:accountId")
    fun getAll(accountId: Long) : List<CustomDomain>

    @Query("SELECT * FROM customDomain WHERE accountId=:accountId AND name IN (:names)")
    fun getAllByNames(names: List<String>, accountId: Long) : List<CustomDomain>

    @Query("SELECT * FROM customDomain WHERE accountId=:accountId")
    fun getCustomDomainByAccountId(accountId: Long) : CustomDomain?

    @Query("SELECT * FROM customDomain WHERE name=:name")
    fun getCustomDomainByName(name: String) : CustomDomain?

    @Query("UPDATE customDomain SET validated = 1 WHERE name = :name")
    fun updateValidated(name: String)

    @Query("DELETE FROM customDomain")
    fun nukeTable()

    @Delete
    fun delete(customDomain: CustomDomain)

    @Delete
    fun deleteAll(customDomain: List<CustomDomain>)

    @Query("""SELECT * FROM customDomain
        WHERE id > :lastId
        AND accountId =:accountId
        ORDER BY id
        LIMIT :limit
    """)
    fun getAllForLinkFile(limit: Int, lastId: Long, accountId:Long) : List<CustomDomain>
}
