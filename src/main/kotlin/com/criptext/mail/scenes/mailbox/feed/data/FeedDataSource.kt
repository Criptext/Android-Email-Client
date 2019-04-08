package com.criptext.mail.scenes.mailbox.feed.data

import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.BackgroundWorkManager
import com.criptext.mail.bgworker.WorkRunner
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.ContactDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.FeedItemDao
import com.criptext.mail.db.dao.FileDao
import com.criptext.mail.db.models.ActiveAccount

/**
 * Created by sebas on 1/24/18.
 */

class FeedDataSource(override val runner: WorkRunner,
                     private val mailboxLocalDB: MailboxLocalDB,
                     private val feedItemLocalDB: FeedItemDao,
                     private val emailDao: EmailDao,
                     private val contactDao: ContactDao,
                     private val fileDao: FileDao,
                     private val activeAccount: ActiveAccount)
    : BackgroundWorkManager<FeedRequest, FeedResult>() {

    override fun createWorkerFromParams(params: FeedRequest, flushResults: (FeedResult) -> Unit): BackgroundWorker<*> {
        return when (params) {
            is FeedRequest.LoadFeed -> LoadFeedsWorker(
                    feedItemLocalDB,
                    emailDao,
                    contactDao,
                    fileDao,
                    params.lastTimeFeedOpened,
                    params.defaultContactName,
                    activeAccount
            ) { result -> flushResults(result) }
            is FeedRequest.DeleteFeedItem -> DeleteFeedItemWorker(feedItemLocalDB,
                    params.item) { result ->
                flushResults(result)
            }
            is FeedRequest.MuteFeedItem -> MuteFeedItemWorker(
                    feedItemLocalDB,
                    params
            ) { result -> flushResults(result) }
            is FeedRequest.GetEmailPreview -> GetEmailPreviewWorker(
                    params.email,
                    activeAccount,
                    mailboxLocalDB,
                    params.userEmail
            ) { result -> flushResults(result) }
        }
    }
}
