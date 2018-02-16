package com.email.scenes.mailbox.feed

import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.feed.ui.FeedItemHolder
import com.email.scenes.mailbox.feed.ui.FeedListController
import com.email.scenes.mailbox.feed.ui.FeedView

/**
 * Created by danieltigse on 2/15/18.
 */

class FeedController(private val model: FeedModel,
                     private val scene: FeedView,
                     private val feedDataSource: FeedDataSource){

    private lateinit var feedListController : FeedListController

    val feedClickListener = object : FeedItemHolder.FeedClickListener{

        override fun onFeedMuted(feedId: Int, position: Int, isMuted: Boolean) {
            feedDataSource.updateFeedItem(feedId, isMuted)
            feedListController.muteFeed(position, isMuted)
        }

        override fun onFeedDeleted(feedId: Int, position: Int) {
            feedDataSource.deleteFeedItem(feedId)
            feedListController.deleteFeed(position)
        }

    }

    fun onStart(feedClickListener: FeedItemHolder.FeedClickListener) {
        feedDataSource.seed()
        val feedItems = feedDataSource.getFeedItems()
        feedListController = FeedListController(model.feedItems, scene)
        feedListController.setFeedList(feedItems)
        scene.toggleNoFeedsView(visible = feedItems.isEmpty())
        scene.feedClickListener = feedClickListener
    }
}