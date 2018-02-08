package com.email.DB.seeders

import com.email.DB.DAO.EmailDao
import com.email.DB.DAO.FeedDao
import com.email.DB.models.Email
import com.email.DB.models.Feed
import com.email.utils.Utility
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

public class FeedSeeder {

    companion object {

        var feeds : List<Feed> = mutableListOf<Feed>()
        var sdf : SimpleDateFormat = SimpleDateFormat( "yyyy-MM-dd HH:mm:dd")

        fun seed(feedDao: FeedDao){
            feeds = feedDao.getAll()
            feedDao.deleteAll(feeds)
            feeds = mutableListOf<Feed>()
            for (a in 1..5){
                feeds += fillFeed(a)
            }
            feedDao.insertAll(feeds)
        }

        fun fillFeed(iteration: Int): Feed {
            lateinit var feed: Feed
            when (iteration) {
                1 -> feed = Feed(1,
                        Utility.FEED_FILE,
                        "Daniel Tigse downloaded",
                        "This is.pdf",
                        sdf.parse("2018-01-02 18:12:29"),
                        false, false)

                2 -> feed = Feed(2,
                        Utility.FEED_MAIL,
                        "Erika Perugachi opened",
                        "RE: Pending report",
                        sdf.parse("2018-01-02 18:12:29"),
                        true, false)
                3 -> feed = Feed(3,
                        Utility.FEED_MAIL,
                        "Brian Cave opened",
                        "RE: Pending report",
                        sdf.parse("2018-01-01 15:12:29"),
                        true, false)
                4 -> feed = Feed(4,
                        Utility.FEED_FILE,
                        "Willian Hudson opened",
                        "Tinting Files.pdf",
                        sdf.parse("2017-11-02 08:12:29"),
                        false, true)
                5 -> feed = Feed(5,
                        Utility.FEED_FILE,
                        "Laura Rivera opened",
                        "central work lab.pdf",
                        sdf.parse("2017-11-01 08:12:29"),
                        false, false)
            }
            return feed
        }
    }
    init {
        sdf.timeZone = TimeZone.getDefault()
    }
}
