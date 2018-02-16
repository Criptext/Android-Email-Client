package com.email.scenes.mailbox.feed.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.email.R
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/7/18.
 */

interface FeedView {

    fun toggleNoFeedsView(visible: Boolean)
    fun notifyItemChanged(index: Int)
    fun notifyDataSetChanged()
    var feedClickListener: FeedItemHolder.FeedClickListener?

    class Default(feedItemsList: VirtualList<ActivityFeedItem>,
                  container: View): FeedView {

        private val viewNoActivity: LinearLayout = container.findViewById(R.id.viewNoActivity)
        private val recyclerViewFeed: RecyclerView = container.findViewById(R.id.recyclerViewFeed)
        private val feedRecyclerView: FeedRecyclerView

        override var feedClickListener: FeedItemHolder.FeedClickListener? = null
            set(value) {
                feedRecyclerView.setFeedClickListener(value)
                field = value
            }

        init {
            feedRecyclerView = FeedRecyclerView(feedItemsList, recyclerViewFeed, feedClickListener)
            if (feedItemsList.size > 0) {
                viewNoActivity.visibility = View.GONE
            }
        }

        override fun notifyItemChanged(index: Int) {
            feedRecyclerView.notifyItemChanged(index)
        }

        override fun notifyDataSetChanged() {
            feedRecyclerView.notifyDataSetChanged()
            if (feedRecyclerView.isFeedListEmpty()) {
                viewNoActivity.visibility = View.VISIBLE
            }
        }

        override fun toggleNoFeedsView(visible: Boolean) {
            viewNoActivity.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

}