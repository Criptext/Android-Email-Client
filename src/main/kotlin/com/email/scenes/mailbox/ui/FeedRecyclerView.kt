package com.email.scenes.mailbox.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.FeedAdapter
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedHolder
import com.email.scenes.search.data.SearchResult

/**
 * Created by danieltigse on 2/5/18.
 */

class FeedRecyclerView(recyclerView: RecyclerView, listener: FeedHolder.FeedClickListener){

    val ctx: Context = recyclerView.context
    private val feedAdapter = FeedAdapter(listener)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = feedAdapter
    }

    fun setFeedList(feeds: List<ActivityFeed>) {
        feedAdapter.feeds= feeds
        notifyThreadSetChanged()
    }

    fun notifyThreadSetChanged() {
        feedAdapter.notifyDataSetChanged()
    }

}