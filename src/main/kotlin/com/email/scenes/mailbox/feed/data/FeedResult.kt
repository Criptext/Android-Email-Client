package com.email.scenes.mailbox.feed.data

import com.email.db.models.FeedItem
/**
 * Created by gabriel on 2/19/18.
 */

sealed class FeedResult {

    sealed class LoadFeed: FeedResult() {
        data class Success(val feedItems: List<FeedItem>): LoadFeed()
        data class Failure(val message: String): LoadFeed()
    }
    sealed class DeleteFeedItem: FeedResult() {
        class Success: DeleteFeedItem()
        data class Failure(val message: String, val item: FeedItem): DeleteFeedItem()
    }

    sealed class MuteFeedItem : FeedResult() {
        class Success: MuteFeedItem()
        data class Failure(val message: String, val id: Int, val lastKnownPosition: Int,
                           val isMuted: Boolean): MuteFeedItem()
    }
}