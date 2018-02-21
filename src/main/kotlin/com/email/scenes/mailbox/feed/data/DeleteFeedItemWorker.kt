package com.email.scenes.mailbox.feed.data

import com.email.DB.FeedLocalDB
import com.email.DB.models.FeedItem
import com.email.bgworker.BackgroundWorker

/**
 * Created by gabriel on 2/20/18.
 */

class DeleteFeedItemWorker(private val db: FeedLocalDB,
                           private val feedItem: FeedItem,
                           override val publishFn: (FeedResult.DeleteFeedItem) -> Unit)
    : BackgroundWorker<FeedResult.DeleteFeedItem> {

    override val canBeParallelized = false

    override fun catchException(ex: Exception): FeedResult.DeleteFeedItem {
        val message = "Unexpected error: " + ex.message
        return FeedResult.DeleteFeedItem.Failure(message, feedItem)
    }

    override fun work(): FeedResult.DeleteFeedItem? {
        db.deleteFeedItem(feedItem.id)
        return FeedResult.DeleteFeedItem.Success()
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}