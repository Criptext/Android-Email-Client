package com.email.DB.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.email.DB.models.FeedItem

/**
 * Created by danieltigse on 2/7/18.
 */

@Dao
interface FeedDao {
    @Insert
    fun insertAll(feedItems: List<FeedItem>)

    @Query("SELECT * FROM feedItem")
    fun getAll() : List<FeedItem>

    @Delete
    fun deleteAll(feedItems: List<FeedItem>)
}