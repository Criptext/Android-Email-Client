package com.email.DB.seeders

import com.email.DB.DAO.FeedDao
import com.email.DB.models.FeedItem
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.utils.Utility
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

public class FeedSeeder {

    companion object {

        var feedItems: List<FeedItem> = mutableListOf<FeedItem>()
        var sdf : SimpleDateFormat = SimpleDateFormat( "yyyy-MM-dd HH:mm:dd")

        fun seed(feedDao: FeedDao){
            feedItems = feedDao.getAll()
            feedDao.deleteAll(feedItems)
            feedItems = mutableListOf<FeedItem>()
            for (a in 1..5){
                feedItems += fillFeed(a)
            }
            feedDao.insertAll(feedItems)
        }

        fun fillFeed(iteration: Int): FeedItem {
            lateinit var feedItem: FeedItem
            when (iteration) {
                1 -> feedItem = FeedItem(1,
                        ActivityFeed.FeedItemTypes.File.ordinal,
                        "Daniel Tigse downloaded",
                        "This is.pdf",
                        sdf.parse("2018-01-02 18:12:29"),
                        false, false)

                2 -> feedItem = FeedItem(2,
                        ActivityFeed.FeedItemTypes.Mail.ordinal,
                        "Erika Perugachi opened",
                        "RE: Pending report",
                        sdf.parse("2018-01-02 18:12:29"),
                        true, false)
                3 -> feedItem = FeedItem(3,
                        ActivityFeed.FeedItemTypes.Mail.ordinal,
                        "Brian Cave opened",
                        "RE: Pending report",
                        sdf.parse("2018-01-01 15:12:29"),
                        true, false)
                4 -> feedItem = FeedItem(4,
                        ActivityFeed.FeedItemTypes.File.ordinal,
                        "Willian Hudson opened",
                        "Tinting Files.pdf",
                        sdf.parse("2017-11-02 08:12:29"),
                        false, true)
                5 -> feedItem = FeedItem(5,
                        ActivityFeed.FeedItemTypes.File.ordinal,
                        "Laura Rivera opened",
                        "central work lab.pdf",
                        sdf.parse("2017-11-01 08:12:29"),
                        false, false)
            }
            return feedItem
        }
    }
    init {
        sdf.timeZone = TimeZone.getDefault()
    }
}
