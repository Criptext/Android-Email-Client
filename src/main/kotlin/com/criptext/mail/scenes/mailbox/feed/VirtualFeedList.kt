package com.criptext.mail.scenes.mailbox.feed

import com.criptext.mail.scenes.mailbox.feed.data.ActivityFeedItem
import com.criptext.mail.utils.virtuallist.VirtualList

class VirtualFeedList(private val model: FeedModel)
    : VirtualList<ActivityFeedItem>{

    override fun get(i: Int): ActivityFeedItem {
        return model.feedItems[i]
    }

    override val size: Int
        get() = model.feedItems.size

    override val hasReachedEnd = model.hasReachedEnd

}