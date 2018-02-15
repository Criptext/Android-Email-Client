package com.email.scenes.mailbox.feed.data

import com.email.DB.models.FeedItem
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

class ActivityFeedItem(private val feedItem: FeedItem){

    enum class FeedItemTypes {
        Mail, File
    }

    val id
        get() = feedItem.id
    val type
        get() = feedItem.feedType
    val title
        get() = feedItem.feedTitle
    val subtitle
        get() = feedItem.feedSubtitle
    val date: Date
        get() = feedItem.feedDate
    val isNew
        get() = feedItem.isNew
    val isMuted
        get() = feedItem.isMuted

}