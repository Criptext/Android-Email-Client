package com.criptext.mail.scenes.mailbox.feed.data

import com.criptext.mail.email_preview.EmailPreview

/**
 * Created by gabriel on 2/19/18.
 */

sealed class FeedResult {

    sealed class GetEmailPreview: FeedResult() {
        data class Success(val emailPreview: EmailPreview,
                           val isTrash: Boolean, val isSpam: Boolean): GetEmailPreview()
        data class Failure(val message: String): GetEmailPreview()
    }

    sealed class LoadFeed: FeedResult() {
        data class Success(val feedItems: List<ActivityFeedItem>, val totalNewFeeds: Int): LoadFeed()
        data class Failure(val message: String): LoadFeed()
    }

    sealed class DeleteFeedItem: FeedResult() {
        class Success: DeleteFeedItem()
        data class Failure(val message: String, val item: ActivityFeedItem): DeleteFeedItem()
    }

    sealed class MuteFeedItem : FeedResult() {
        class Success: MuteFeedItem()
        data class Failure(val message: String, val id: Long, val lastKnownPosition: Int,
                           val isMuted: Boolean): MuteFeedItem()
    }
}