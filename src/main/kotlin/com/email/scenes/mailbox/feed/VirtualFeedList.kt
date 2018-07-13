package com.email.scenes.mailbox.feed

import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.utils.virtuallist.VirtualList

class VirtualFeedList(private val model: FeedModel)
    : VirtualList<ActivityFeedItem>{

    override fun get(i: Int): ActivityFeedItem {
        return model.feedItems[i]
    }

    override val size: Int
        get() = model.feedItems.size

    override val hasReachedEnd = model.hasReachedEnd

}