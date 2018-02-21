package com.email.DB.DAO

import android.arch.persistence.room.*
import com.email.DB.models.Email
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

    @Query("UPDATE feedItem " +
            "SET isMuted = :isMuted " +
            "where id=:id")
    fun toggleMute(id: Int, isMuted: Boolean)

    @Query("DELETE FROM feedItem " +
            "where id=:id")
    fun delete(id: Int)
}