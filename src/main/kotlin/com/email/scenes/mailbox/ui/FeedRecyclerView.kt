package com.email.scenes.mailbox.ui

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.FeedItemAdapter
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedItemHolder

/**
 * Created by danieltigse on 2/5/18.
 */

class FeedRecyclerView(recyclerView: RecyclerView, listener: FeedItemHolder.FeedClickListener){

    val ctx: Context = recyclerView.context
    private val feedAdapter = FeedItemAdapter(listener)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = feedAdapter
    }

    fun setFeedList(feeds: MutableList<ActivityFeed>) {
        feedAdapter.feeds= feeds
        notifyFeedSetChanged()
    }

    fun notifyFeedSetChanged() {
        feedAdapter.notifyDataSetChanged()
    }

    fun notifyItemChanged(activityFeed: ActivityFeed) {
        val index = feedAdapter.feeds.indexOf(activityFeed)
        feedAdapter.feeds[index] = activityFeed
        feedAdapter.notifyItemChanged(index)
    }

    fun notifyItemRemoved(activityFeed: ActivityFeed) {
        val index = feedAdapter.feeds.indexOf(activityFeed)
        feedAdapter.feeds.remove(activityFeed)
        feedAdapter.notifyItemRemoved(index)
    }

    fun isFeedListEmpty(): Boolean{
        return feedAdapter.feeds.isEmpty()
    }

}