package com.email.scenes.mailbox.feed.data

import com.email.db.models.Email
import com.email.db.models.FeedItem

/**
 * Created by gabriel on 2/20/18.
 */
sealed class FeedRequest {
    data class GetEmailPreview(val email: Email, val userEmail: String): FeedRequest()
    data class LoadFeed(val lastTimeFeedOpened: Long): FeedRequest()
    data class DeleteFeedItem(val item: ActivityFeedItem, val position: Int): FeedRequest()
    data class MuteFeedItem(val id: Long, val position: Int, val isMuted: Boolean): FeedRequest()
}