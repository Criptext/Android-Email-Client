package com.email.scenes.mailbox.ui

import android.support.design.widget.NavigationView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.email.R
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.scenes.mailbox.feed.ui.FeedItemHolder
import com.email.scenes.mailbox.feed.ui.FeedRecyclerView
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerFeedView(feedItemsList: VirtualList<ActivityFeedItem>, navigationView: NavigationView){

    private val viewNoActivity : LinearLayout
    private val recyclerViewFeed: RecyclerView

    private var feedRecyclerView: FeedRecyclerView

    var feedClickListener: FeedItemHolder.FeedClickListener? = null
        set(value) {
            feedRecyclerView.setFeedClickListener(value)
            field = value
        }

    init {
        viewNoActivity = navigationView.findViewById<LinearLayout>(R.id.viewNoActivity)
        recyclerViewFeed = navigationView.findViewById(R.id.recyclerViewFeed)
        feedRecyclerView = FeedRecyclerView(feedItemsList, recyclerViewFeed, feedClickListener)
        if(feedItemsList.size > 0){
            viewNoActivity.visibility = View.GONE
        }
    }

    fun notifyItemChanged(index: Int) {
        feedRecyclerView.notifyItemChanged(index)
    }

    fun notifyDataSetChanged() {
        feedRecyclerView.notifyDataSetChanged()
        if(feedRecyclerView.isFeedListEmpty()){
            viewNoActivity.visibility = View.VISIBLE
        }
    }

    fun showViewNoFeeds(show: Boolean){
        viewNoActivity.visibility = if (show) View.VISIBLE else View.GONE
    }

}