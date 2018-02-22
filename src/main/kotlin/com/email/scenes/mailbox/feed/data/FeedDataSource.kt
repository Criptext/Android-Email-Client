package com.email.scenes.mailbox.feed.data

import com.email.DB.FeedLocalDB
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.WorkHandler
import com.email.bgworker.WorkRunner

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(override val runner: WorkRunner, private val feedLocalDB: FeedLocalDB)
    : WorkHandler<FeedRequest, FeedResult>() {

    override fun createWorkerFromParams(params: FeedRequest, flushResults: (FeedResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is FeedRequest.LoadFeed -> LoadFeedsWorker(feedLocalDB, { result ->
                flushResults(result)
            })
            is FeedRequest.DeleteFeedItem -> DeleteFeedItemWorker(feedLocalDB, params.item, { result ->
                flushResults(result)
            })
            is FeedRequest.MuteFeedItem -> MuteFeedItemWorker(
                    feedLocalDB,
                    params,
                    { result -> flushResults(result) }
            )
        }
    }
}
