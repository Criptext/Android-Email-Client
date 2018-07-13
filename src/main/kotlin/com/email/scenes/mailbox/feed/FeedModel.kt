package com.email.scenes.mailbox.feed

import com.email.scenes.mailbox.feed.data.ActivityFeedItem

/**
 * Created by danieltigse on 2/15/18.
 */

class FeedModel{
    val feedItems: ArrayList<ActivityFeedItem> = ArrayList()
    var hasReachedEnd = true
}