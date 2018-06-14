package com.email.scenes.mailbox.feed.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.email.utils.virtuallist.VirtualList

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedItemAdapter(val feedItemsList: VirtualList<ActivityFeedItem>,
                      var listener: FeedItemHolder.FeedClickListener?): RecyclerView.Adapter<FeedItemHolder>() {

    private val viewBinderHelper = ViewBinderHelper()

    init {
        viewBinderHelper.setOpenOnlyOne(true)
        setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: FeedItemHolder, position: Int) {
        if(holder != null) {
            val feedItem = feedItemsList[position]
            holder.bindFeed(feedItem, position, listener)
            viewBinderHelper.bind(holder.getSwipeView(), feedItem.id.toString())
        }
    }

    override fun getItemId(position: Int) = feedItemsList[position].id!!.toLong()

    override fun getItemCount(): Int {
        return feedItemsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedItemHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedItemHolder(inflatedView)
    }

}
