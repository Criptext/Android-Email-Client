package com.email.scenes.mailbox.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.DrawerFeedView
import com.email.scenes.mailbox.FeedItemAdapter
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedItemHolder
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/5/18.
 */

class FeedRecyclerView(feedsList: VirtualList<ActivityFeed>, recyclerView: RecyclerView, listener: FeedItemHolder.FeedClickListener){

    val ctx: Context = recyclerView.context
    private val feedAdapter = FeedItemAdapter(feedsList, listener)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = feedAdapter
    }

    fun notifyFeedSetChanged() {
        feedAdapter.notifyDataSetChanged()
    }

    fun notifyItemChanged(index: Int) {
        feedAdapter.notifyItemChanged(index)
    }

    fun notifyItemRemoved(index: Int) {
        feedAdapter.notifyItemRemoved(index)
    }

    fun isFeedListEmpty(): Boolean{
        return feedAdapter.feedsList.size == 0
    }

}