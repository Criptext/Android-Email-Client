package com.criptext.mail.scenes.mailbox.feed.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.criptext.mail.R
import com.criptext.mail.scenes.mailbox.feed.VirtualFeedList
import com.criptext.mail.utils.ui.EmptyViewHolder
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedItemAdapter(private val feedItemsList: VirtualFeedList,
                      private val lastTimeFeedOpened: Long,
                      private var listener: FeedItemHolder.FeedEventListener?)
    : VirtualListAdapter(feedItemsList) {

    private val viewBinderHelper = ViewBinderHelper()

    init {
        viewBinderHelper.setOpenOnlyOne(true)
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder){
            is FeedItemHolder -> {
                val feedItem = feedItemsList[position]
                holder.bindFeed(
                        lastTimeFeedOpened = lastTimeFeedOpened,
                        activityFeedItem = feedItem,
                        position = position,
                        listener = listener)
                viewBinderHelper.bind(holder.getSwipeView(), feedItem.id.toString())
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedItemHolder(inflatedView)
    }

    override fun createEmptyViewHolder(parent: ViewGroup): EmptyViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_no_feeds, parent,
                        false)
        return EmptyViewHolder(inflatedView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {
        listener?.onApproachingEnd()
    }

    override fun getActualItemId(position: Int): Long {
        return feedItemsList[position].id
    }

}
