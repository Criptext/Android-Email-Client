package com.email.scenes.mailbox.feed.ui

import com.email.db.models.FeedItem
import com.email.utils.addWhere
import com.email.utils.findFromPosition

/**
 * Created by danieltigse on 02/14/18.
 */

class FeedListController(private val feedItems: ArrayList<FeedItem>,
                         private val scene: FeedView) {

    fun refreshFeedItems(feedItems: List<FeedItem>) {
        if (feedItems.isNotEmpty()) {
            this.feedItems.clear()
            this.feedItems.addAll(feedItems)
            scene.notifyDataSetChanged()
        }
        toggleNoFeedItemsView()
    }

    fun toggleMutedFeedItem(id: Int, lastPosition: Int, isMuted: Boolean) {
        val index = feedItems.findFromPosition(lastPosition, { feedItem -> feedItem.id == id  })
        if (index > -1) {
            val feedItem = feedItems[index]
            feedItem.isMuted = isMuted
            scene.notifyItemChanged(index)
        }
    }

    fun insertFeedItem(newItem: FeedItem) {
        val pos = feedItems.addWhere(newItem, { existingItem ->
            newItem.feedDate > existingItem.feedDate
        })
        scene.notifyItemInserted(pos)
        toggleNoFeedItemsView()
    }

    private fun toggleNoFeedItemsView() {
        scene.toggleNoFeedsView(visible = feedItems.isEmpty())
    }

    fun deleteFeedItem(id: Int, lastPosition: Int): FeedItem? {
        val index = feedItems.findFromPosition(lastPosition, { feedItem -> feedItem.id == id  })
        return if (index > -1) {
            val deleted = feedItems.removeAt(index)
            scene.notifyItemRemoved(index)
            toggleNoFeedItemsView()
            deleted
        } else null
    }
}
