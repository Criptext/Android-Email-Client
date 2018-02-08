package com.email.scenes.mailbox.data

import com.email.DB.FeedLocalDB

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(private val feedLocalDB: FeedLocalDB) {

    fun getFeeds(): List<ActivityFeed> {
        return feedLocalDB.getFeeds()
    }

    fun seed() {
        feedLocalDB.seed()
    }

    fun deleteFeed(activityFeed: ActivityFeed) {
        feedLocalDB.deleteFeed(activityFeed)
    }

    fun updateFeed(activityFeed: ActivityFeed){

    }
}
