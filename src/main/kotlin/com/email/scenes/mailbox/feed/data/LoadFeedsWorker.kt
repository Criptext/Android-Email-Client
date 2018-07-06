package com.email.scenes.mailbox.feed.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.ContactDao
import com.email.db.dao.EmailDao
import com.email.db.dao.FeedItemDao
import com.email.db.dao.FileDao

/**
 * Created by gabriel on 2/19/18.
 */

class LoadFeedsWorker(private val feedItemDao: FeedItemDao,
                      private val emailDao: EmailDao,
                      private val contactDao: ContactDao,
                      private val fileDao: FileDao,
                      private val lastTimeFeedOpened: Long,
                      override val publishFn: (FeedResult.LoadFeed) -> Unit)
    : BackgroundWorker<FeedResult.LoadFeed> {

    override fun catchException(ex: Exception): FeedResult.LoadFeed {
        val message = "Unexpected error: " + ex.message
        return FeedResult.LoadFeed.Failure(message)
    }

    override val canBeParallelized = true

    override fun work(reporter: ProgressReporter<FeedResult.LoadFeed>): FeedResult.LoadFeed {
        val items = feedItemDao.getAllFeedItems()
        val activityFeedItems = items.map {
            ActivityFeedItem(
                    feedItem = it,
                    email = emailDao.findEmailById(it.emailId)!!,
                    contact = contactDao.getContactById(it.contactId)!!,
                    file = if(it.fileId != null) fileDao.getFileById(it.fileId!!) else null
            )
        }
        val totalNewFeeds = activityFeedItems.fold(0, { total, next ->
            total + (if(next.date.time > lastTimeFeedOpened) 1 else 0)
        })
        return FeedResult.LoadFeed.Success(activityFeedItems, totalNewFeeds)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }

}