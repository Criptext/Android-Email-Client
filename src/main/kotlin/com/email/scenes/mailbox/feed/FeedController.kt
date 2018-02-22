package com.email.scenes.mailbox.feed

import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.feed.data.FeedRequest
import com.email.scenes.mailbox.feed.data.FeedResult
import com.email.scenes.mailbox.feed.ui.FeedItemHolder
import com.email.scenes.mailbox.feed.ui.FeedListController
import com.email.scenes.mailbox.feed.ui.FeedView

/**
 * Created by danieltigse on 2/15/18.
 */

class FeedController(private val model: FeedModel,
                     private val scene: FeedView,
                     private val feedDataSource: FeedDataSource){

    private val feedListController = FeedListController(model.feedItems, scene)

    private val feedClickListener = object : FeedItemHolder.FeedClickListener{

        override fun onMuteFeedItemClicked(feedId: Int, position: Int, isMuted: Boolean) {
            feedListController.toggleMutedFeedItem(id = feedId,
                    lastPosition = position,
                    isMuted = isMuted)
            feedDataSource.submitRequest(FeedRequest.MuteFeedItem(id = feedId,
                    position = position,
                    isMuted = isMuted))
        }

        override fun onDeleteFeedItemClicked(feedId: Int, position: Int) {
            val deleted = feedListController.deleteFeedItem(id = feedId, lastPosition = position)
            if (deleted != null) {
                val req = FeedRequest.DeleteFeedItem(item = deleted, position = position)
                feedDataSource.submitRequest(req)
            }
        }
    }

    private val dataSourceListener = { result: FeedResult ->
        when (result) {
            is FeedResult.LoadFeed -> onFeedItemsLoaded(result)
            is FeedResult.DeleteFeedItem -> onFeedItemDeleted(result)
            is FeedResult.MuteFeedItem -> onFeedItemMuted(result)
        }
    }

    private fun onFeedItemsLoaded(result: FeedResult.LoadFeed) {
        when (result) {
            is FeedResult.LoadFeed.Success -> feedListController.refreshFeedItems(result.feedItems)
            is FeedResult.LoadFeed.Failure -> scene.showError(result.message)
        }
    }

    private fun onFeedItemDeleted(result: FeedResult.DeleteFeedItem) {
        when (result) {
            is FeedResult.DeleteFeedItem.Success -> {/* NoOp */}
            is FeedResult.DeleteFeedItem.Failure -> {
                feedListController.insertFeedItem(result.item)
                scene.showError(result.message)
            }
        }
    }

    private fun onFeedItemMuted(result: FeedResult.MuteFeedItem) {
        when (result) {
            is FeedResult.MuteFeedItem.Success -> {/* NoOp */}
            is FeedResult.MuteFeedItem.Failure -> {
                feedListController.toggleMutedFeedItem(id = result.id,
                        lastPosition = result.lastKnownPosition,
                        isMuted = result.isMuted)
                scene.showError(result.message)
            }
        }
    }

    fun onStart() {
        val isEmpty = model.feedItems.isEmpty()
        scene.toggleNoFeedsView(visible = isEmpty)
        if (isEmpty)
            feedDataSource.submitRequest(FeedRequest.LoadFeed())

        scene.feedClickListener = feedClickListener
        feedDataSource.listener = dataSourceListener
    }

    fun onStop() {
        scene.feedClickListener = null
        feedDataSource.listener = null
    }
}