package com.email.DB

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.email.DB.seeders.FeedSeeder
import com.email.scenes.mailbox.data.ActivityFeed

/**
 * Created by danieltigse on 2/7/18.
 */

interface FeedLocallDB{

    fun getFeeds(): List<ActivityFeed>
    fun seed()

    class Default(applicationContext: Context): FeedLocallDB{

        private val db = AppDatabase.getAppDatabase(applicationContext)

        override fun getFeeds(): List<ActivityFeed> {
            return db!!.feedDao().getAll().map { feed ->
                ActivityFeed(feed.feedType, feed.feedTitle, feed.feedSubtitle, feed.feedDate, feed.isNew, feed.isMuted)
            }
        }

        override fun seed() {
            try {
                FeedSeeder.seed(db!!.feedDao())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}