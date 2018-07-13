package com.email.scenes.mailbox.feed.ui

import com.email.scenes.mailbox.feed.FeedModel
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.utils.addWhere
import com.email.utils.findFromPosition
import com.email.utils.virtuallist.VirtualListView

/**
 * Created by danieltigse on 02/14/18.
 */

class FeedListController(private val model: FeedModel,
                         private val virtualListView: VirtualListView?) {

    fun clear(){
        model.feedItems.clear()
        virtualListView?.notifyDataSetChanged()
    }

    fun populateFeeds(feedItems: List<ActivityFeedItem>){
        model.feedItems.clear()
        model.feedItems.addAll(feedItems)
    }

    fun refreshFeedItems(feedItems: List<ActivityFeedItem>) {
        if (feedItems.isNotEmpty()) {
            model.feedItems.clear()
            model.feedItems.addAll(feedItems)
            virtualListView?.notifyDataSetChanged()
        }
    }

    fun toggleMutedFeedItem(id: Long, lastPosition: Int) {
        val index = model.feedItems.findFromPosition(lastPosition, { feedItem -> feedItem.id == id  })
        if (index > -1) {
            virtualListView?.notifyItemChanged(index)
        }
    }

    fun insertFeedItem(newItem: ActivityFeedItem) {
        val pos = model.feedItems.addWhere(newItem, { existingItem ->
            newItem.date > existingItem.date
        })
        virtualListView?.notifyItemRangeInserted(pos, model.feedItems.size)
    }

    fun deleteFeedItem(id: Long, lastPosition: Int): ActivityFeedItem? {
        val index = model.feedItems.findFromPosition(lastPosition, { feedItem -> feedItem.id == id  })
        return if (index > -1) {
            val deleted = model.feedItems.removeAt(index)
            virtualListView?.notifyItemRemoved(index)
            deleted
        } else null
    }
}
