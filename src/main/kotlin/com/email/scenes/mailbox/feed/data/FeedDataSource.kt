package com.email.scenes.mailbox.feed.data

import com.email.bgworker.BackgroundWorker
import com.email.bgworker.BackgroundWorkManager
import com.email.bgworker.WorkRunner
import com.email.db.MailboxLocalDB
import com.email.db.dao.ContactDao
import com.email.db.dao.EmailDao
import com.email.db.dao.FeedItemDao
import com.email.db.dao.FileDao

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(override val runner: WorkRunner,
                     private val mailboxLocalDB: MailboxLocalDB,
                     private val feedItemLocalDB: FeedItemDao,
                     private val emailDao: EmailDao,
                     private val contactDao: ContactDao,
                     private val fileDao: FileDao)
    : BackgroundWorkManager<FeedRequest, FeedResult>() {

    override fun createWorkerFromParams(params: FeedRequest, flushResults: (FeedResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is FeedRequest.LoadFeed -> LoadFeedsWorker(
                    feedItemLocalDB,
                    emailDao,
                    contactDao,
                    fileDao,
                    params.lastTimeFeedOpened,
                    { result -> flushResults(result) }
            )
            is FeedRequest.DeleteFeedItem -> DeleteFeedItemWorker(feedItemLocalDB,
                    params.item, { result ->
                flushResults(result)
            })
            is FeedRequest.MuteFeedItem -> MuteFeedItemWorker(
                    feedItemLocalDB,
                    params,
                    { result -> flushResults(result) }
            )
            is FeedRequest.GetEmailPreview -> GetEmailPreviewWorker(
                    params.email,
                    mailboxLocalDB,
                    params.userEmail,
                    { result -> flushResults(result) }
            )
        }
    }
}
