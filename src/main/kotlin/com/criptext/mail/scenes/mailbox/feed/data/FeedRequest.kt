package com.criptext.mail.scenes.mailbox.feed.data

import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.FeedItem

/**
 * Created by gabriel on 2/20/18.
 */
sealed class FeedRequest {
    data class GetEmailPreview(val email: Email, val userEmail: String): FeedRequest()
    data class LoadFeed(val lastTimeFeedOpened: Long): FeedRequest()
    data class DeleteFeedItem(val item: ActivityFeedItem, val position: Int): FeedRequest()
    data class MuteFeedItem(val id: Long, val position: Int, val isMuted: Boolean): FeedRequest()
}