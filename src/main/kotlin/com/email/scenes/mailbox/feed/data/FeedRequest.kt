package com.email.scenes.mailbox.feed.data

import com.email.DB.models.FeedItem

/**
 * Created by gabriel on 2/20/18.
 */
sealed class FeedRequest {
    class LoadFeed: FeedRequest()
    data class DeleteFeedItem(val item: FeedItem, val position: Int): FeedRequest()
    data class MuteFeedItem(val id: Int, val position: Int, val isMuted: Boolean): FeedRequest()
}