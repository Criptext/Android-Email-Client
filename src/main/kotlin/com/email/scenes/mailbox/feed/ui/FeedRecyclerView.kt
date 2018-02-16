package com.email.scenes.mailbox.feed.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.utils.CustomLayoutManager
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/5/18.
 */

class FeedRecyclerView(feedItemsList: VirtualList<ActivityFeedItem>, recyclerView: RecyclerView,
                       listener: FeedItemHolder.FeedClickListener?){

    val ctx: Context = recyclerView.context
    private val feedAdapter = FeedItemAdapter(feedItemsList, listener)

    init {
        recyclerView.layoutManager = CustomLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = feedAdapter
    }

    fun setFeedClickListener(feedClickListener: FeedItemHolder.FeedClickListener?){
        feedAdapter.listener = feedClickListener
    }

    fun notifyItemChanged(index: Int) {
        feedAdapter.notifyItemChanged(index)
    }

    fun notifyDataSetChanged(){
        feedAdapter.notifyDataSetChanged()
    }

    fun isFeedListEmpty(): Boolean{
        return feedAdapter.feedItemsList.size == 0
    }

}