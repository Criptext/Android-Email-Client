package com.criptext.mail.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.criptext.mail.db.models.AntiPushMap
import com.criptext.mail.db.models.signal.CRSignedPreKey

/**
 * Created by gabriel on 3/6/18.
 */

@Dao
interface AntiPushMapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(antiPushMap: AntiPushMap): Long

    @Query("SELECT id FROM antiPushMap WHERE value=:value AND accountId=:accountId")
    fun getByValue(value: String, accountId: Long): Int?

    @Query("DELETE FROM antiPushMap WHERE id=:notificationId")
    fun deleteById(notificationId: Int)

    @Query("DELETE FROM antiPushMap WHERE accountId = :accountId")
    fun deleteAll(accountId: Long)

}