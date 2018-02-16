package com.email.scenes.mailbox.feed.ui

import com.email.DB.models.FeedItem

/**
 * Created by danieltigse on 02/14/18.
 */

class FeedListController(private val feedItems: ArrayList<FeedItem>,
                         private val scene: FeedView) {

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
