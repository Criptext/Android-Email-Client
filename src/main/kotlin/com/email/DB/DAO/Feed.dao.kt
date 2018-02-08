package com.email.DB.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.DB.models.Feed
import com.email.DB.models.Label

/**
 * Created by danieltigse on 2/7/18.
 */

@Dao
interface FeedDao {
    @Insert
    fun insertAll(feeds: List<Feed>)

    @Query("SELECT * FROM feed")
    fun getAll() : List<Feed>

    @Delete
    fun deleteAll(feeds: List<Feed>)
}