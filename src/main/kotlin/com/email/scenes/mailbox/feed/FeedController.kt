package com.email.scenes.mailbox.feed

import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.feed.ui.FeedItemHolder
import com.email.scenes.mailbox.feed.ui.FeedListController
import com.email.scenes.mailbox.ui.DrawerFeedView

/**
 * Created by danieltigse on 2/15/18.
 */

class FeedController(private val model: FeedModel,
                     private val feedDataSource: FeedDataSource){

    private lateinit var feedListController : FeedListController
    private lateinit var scene: DrawerFeedView

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

    fun onStart(scene: DrawerFeedView, feedClickListener: FeedItemHolder.FeedClickListener) {
        this.scene = scene
        feedDataSource.seed()
        val feedItems = feedDataSource.getFeedItems()
        feedListController = FeedListController(model.feedItems, scene)
        feedListController.setFeedList(feedItems)
        scene.showViewNoFeeds(feedItems.isEmpty())
        scene.feedClickListener = feedClickListener
    }
}