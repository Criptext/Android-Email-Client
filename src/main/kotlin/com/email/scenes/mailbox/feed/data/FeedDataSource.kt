package com.email.scenes.mailbox.feed.data

import com.email.DB.FeedLocalDB
import com.email.DB.models.FeedItem

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(private val feedLocalDB: FeedLocalDB) {

    fun getFeedItems(): List<FeedItem> {
        return feedLocalDB.getFeedItems()
    }

    fun seed() {
        feedLocalDB.seed()
    }

    fun deleteFeedItem(id: Int) {
        feedLocalDB.deleteFeedItem(id)
    }

    fun updateFeedItem(id: Int, isMuted: Boolean){
        feedLocalDB.updateFeedItem(id, isMuted)
    }
}
