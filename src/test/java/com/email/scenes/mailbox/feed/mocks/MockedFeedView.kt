package com.email.scenes.mailbox.feed.mocks

import com.email.scenes.mailbox.feed.ui.FeedItemHolder
import com.email.scenes.mailbox.feed.ui.FeedView

/**
 * Created by gabriel on 2/21/18.
 */

class MockedFeedView: FeedView {
    var shownError: String? = null
    var isNoFeedsViewVisible = false
    var notifiedDataSetChanged = false
    var lastNotifiedChangedPosition = -1
    var lastNotifiedRemovedPosition = -1
    var lastNotifiedInsertedPosition = -1

    override fun toggleNoFeedsView(visible: Boolean) {
        isNoFeedsViewVisible = visible
    }

    override fun notifyItemChanged(index: Int) {
        lastNotifiedChangedPosition = index
    }

    override fun notifyItemRemoved(index: Int) {
        lastNotifiedRemovedPosition = index
    }

    override fun notifyItemInserted(index: Int) {
        lastNotifiedInsertedPosition = index
    }

    override fun notifyDataSetChanged() {
        notifiedDataSetChanged = true
    }

    override fun showError(errorMessage: String) {
        shownError = errorMessage
    }

    override var feedClickListener: FeedItemHolder.FeedClickListener? = null

}