package com.criptext.mail.db.dao

import androidx.room.*
import com.criptext.mail.db.models.FeedItem

/**
 * Created by danieltigse on 2/7/18.
 */

@Dao
interface FeedItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFeedItem(feedItem: FeedItem)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFeedItems(feedItems: List<FeedItem>)

    @Query("""SELECT * FROM feedItem
        WHERE emailId IN
        (SELECT id FROM email WHERE accountId=:accountId)
        ORDER BY date DESC""")
    fun getAllFeedItems(accountId: Long) : List<FeedItem>

    @Delete
    fun deleteFeedItems(feedItems: List<FeedItem>)

    @Query("""DELETE FROM feedItem
           WHERE id=:id""")
    fun deleteFeedItemById(id: Long)

    @Query("DELETE FROM feedItem")
    fun nukeTable()
}