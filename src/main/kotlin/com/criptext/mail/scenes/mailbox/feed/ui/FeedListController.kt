package com.criptext.mail.scenes.mailbox.feed.ui

import com.criptext.mail.scenes.mailbox.feed.FeedModel
import com.criptext.mail.scenes.mailbox.feed.data.ActivityFeedItem
import com.criptext.mail.utils.addWhere
import com.criptext.mail.utils.findFromPosition
import com.criptext.mail.utils.virtuallist.VirtualListView

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
