package com.email.scenes.mailbox.feed.mocks

import com.email.db.dao.FeedDao
import com.email.db.models.FeedItem

/**
 * Created by gabriel on 2/21/18.
 */
class MockedFeedDao : FeedDao {
    var nextLoadedFeedItems: List<FeedItem>? = null
    var nextMuteFeedItemException: Exception? = null
    var nextDeleteFeedItemException: Exception? = null
    override fun insertFeedItems(feedItems: List<FeedItem>) {
    }

    override fun getAllFeedItems(): List<FeedItem> {
        return nextLoadedFeedItems!!
    }

    override fun deleteFeedItems(feedItems: List<FeedItem>) {
    }

    override fun toggleMuteFeedItem(id: Int, isMuted: Boolean) {
        val ex = nextMuteFeedItemException
        if (ex != null)
            throw ex
    }

    override fun deleteFeedItemById(id: Int) {
        val ex = nextDeleteFeedItemException
        if (ex != null)
            throw ex
    }

}