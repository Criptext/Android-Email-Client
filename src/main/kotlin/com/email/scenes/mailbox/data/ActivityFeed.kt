package com.email.scenes.mailbox.data

import com.email.DB.models.FeedItem
import java.util.*

/**
 * Created by danieltigse on 2/7/18.
 */

class ActivityFeed(val feedItem: FeedItem){

    enum class FeedItemTypes {
        Mail, File
    }

    val feedId = feedItem.id
    val feedType = feedItem.feedType
    val feedTitle = feedItem.feedTitle
    val feedSubtitle = feedItem.feedSubtitle
    val feedDate: Date = feedItem.feedDate
    var isNew = feedItem.isNew
    var isMuted = feedItem.isMuted

}