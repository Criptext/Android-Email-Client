package com.email.db.dao

import android.arch.persistence.room.*
import com.email.db.models.FeedItem

/**
 * Created by danieltigse on 2/7/18.
 */

@Dao
interface FeedDao {
    @Insert
    fun insertFeedItems(feedItems: List<FeedItem>)

    @Query("SELECT * FROM feedItem")
    fun getAllFeedItems() : List<FeedItem>

    @Delete
    fun deleteFeedItems(feedItems: List<FeedItem>)

    @Query("""UPDATE feedItem
            SET isMuted = :isMuted
            WHERE id=:id""")
    fun toggleMuteFeedItem(id: Int, isMuted: Boolean)

    @Query("""DELETE FROM feedItem
           WHERE id=:id""")
    fun deleteFeedItemById(id: Int)
}