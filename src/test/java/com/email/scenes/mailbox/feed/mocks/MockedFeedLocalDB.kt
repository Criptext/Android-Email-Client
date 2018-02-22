package com.email.scenes.mailbox.feed.mocks

import com.email.DB.FeedLocalDB
import com.email.DB.models.FeedItem

/**
 * Created by gabriel on 2/21/18.
 */
class MockedFeedLocalDB: FeedLocalDB {

    var nextLoadedFeedItems: List<FeedItem>? = null
    var nextMuteFeedItemException: Exception? = null
    var nextDeleteFeedItemException: Exception? = null

    override fun getFeedItems(): List<FeedItem> {
        return nextLoadedFeedItems!!
    }

    override fun seed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteFeedItem(id: Int) {
        val ex = nextDeleteFeedItemException
        if (ex != null)
            throw ex
    }

    override fun muteFeedItem(id: Int, isMuted: Boolean) {
        val ex = nextMuteFeedItemException
        if (ex != null)
            throw ex
    }

}