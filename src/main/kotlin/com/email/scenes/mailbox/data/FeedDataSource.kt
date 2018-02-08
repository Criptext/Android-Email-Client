package com.email.scenes.mailbox.data

import com.email.DB.FeedLocallDB
import com.email.DB.MailboxLocalDB

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(private val feedLocallDB: FeedLocallDB) {

    fun getFeeds(): List<ActivityFeed> {
        return feedLocallDB.getFeeds()
    }

    fun seed() {
        feedLocallDB.seed()
    }
}
