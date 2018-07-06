package com.email.scenes.mailbox.feed.data

import com.email.db.models.FeedItem
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.FeedItemDao

/**
 * Created by gabriel on 2/20/18.
 */

class DeleteFeedItemWorker(private val db: FeedItemDao,
                           private val feedItem: ActivityFeedItem,
                           override val publishFn: (FeedResult.DeleteFeedItem) -> Unit)
    : BackgroundWorker<FeedResult.DeleteFeedItem> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): FeedResult.DeleteFeedItem {
        val message = "Unexpected error: " + ex.message
        return FeedResult.DeleteFeedItem.Failure(message, feedItem)
    }

    override fun work(reporter: ProgressReporter<FeedResult.DeleteFeedItem>): FeedResult.DeleteFeedItem? {
        db.deleteFeedItemById(feedItem.id)
        return FeedResult.DeleteFeedItem.Success()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use CRFile | Settings | CRFile Templates.
    }
}