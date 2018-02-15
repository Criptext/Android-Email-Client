package com.email.scenes.mailbox.feed.ui

import com.email.DB.models.FeedItem
import com.email.scenes.mailbox.ui.DrawerFeedView

/**
 * Created by danieltigse on 02/14/18.
 */

class FeedListController(private val feedItems: ArrayList<FeedItem>,
                         private val scene: DrawerFeedView) {

    fun setFeedList(feedItems: List<FeedItem>) {
        this.feedItems.clear()
        this.feedItems.addAll(feedItems)
    }

    fun muteFeed(position: Int, isMuted: Boolean){
        feedItems[position].isMuted = isMuted
        scene.notifyItemChanged(position)
    }

    fun deleteFeed(position: Int){
        feedItems.removeAt(position)
        scene.notifyDataSetChanged()
    }
}
