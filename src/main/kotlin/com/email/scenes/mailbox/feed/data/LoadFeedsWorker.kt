package com.email.scenes.mailbox.feed.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.FeedDao

/**
 * Created by gabriel on 2/19/18.
 */

class LoadFeedsWorker(private val db: FeedDao,
                      override val publishFn: (FeedResult.LoadFeed) -> Unit)
    : BackgroundWorker<FeedResult.LoadFeed> {

    override fun catchException(ex: Exception): FeedResult.LoadFeed {
        val message = "Unexpected error: " + ex.message
        return FeedResult.LoadFeed.Failure(message)
    }

    override val canBeParallelized = true

    override fun work(reporter: ProgressReporter<FeedResult.LoadFeed>): FeedResult.LoadFeed {
        val items = db.getAllFeedItems()
        return FeedResult.LoadFeed.Success(items)
    }

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}