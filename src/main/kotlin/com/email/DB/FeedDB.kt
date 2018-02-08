package com.email.DB

import android.content.Context
import com.email.DB.seeders.FeedSeeder
import com.email.scenes.mailbox.data.ActivityFeed

/**
 * Created by danieltigse on 2/7/18.
 */

interface FeedLocalDB {

    fun getFeeds(): List<ActivityFeed>
    fun seed()
    fun deleteFeed(activityFeed: ActivityFeed)
    fun updateFeed(activityFeed: ActivityFeed)

    class Default(applicationContext: Context): FeedLocalDB {

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getFeeds(): List<ActivityFeed> {
            return db.feedDao().getAll().map { feed ->
                ActivityFeed(feed)
            }
        }

        override fun seed() {
            try {
                FeedSeeder.seed(db.feedDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun deleteFeed(activityFeed: ActivityFeed) {
            db.feedDao().deleteAll(listOf(activityFeed.feedItem))
        }

        override fun updateFeed(activityFeed: ActivityFeed) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

}