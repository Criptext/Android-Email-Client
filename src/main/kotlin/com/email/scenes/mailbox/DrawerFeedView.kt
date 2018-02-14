package com.email.scenes.mailbox

import android.support.design.widget.NavigationView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedItemHolder
import com.email.scenes.mailbox.ui.FeedRecyclerView
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerFeedView(feedsList: VirtualList<ActivityFeed>, navigationView: NavigationView, feedClickListener: FeedItemHolder.FeedClickListener){

    private val viewNoActivity : LinearLayout
    private val recyclerViewFeed: RecyclerView

    private var feedRecyclerView: FeedRecyclerView

    init {
        viewNoActivity = navigationView.findViewById(R.id.viewNoActivity)
        recyclerViewFeed = navigationView.findViewById(R.id.recyclerViewFeed)
        feedRecyclerView = FeedRecyclerView(feedsList, recyclerViewFeed, feedClickListener)
        if(feedsList.size > 0){
            viewNoActivity.visibility = View.GONE
        }
    }

    fun notifyItemChanged(index: Int) {
        feedRecyclerView.notifyItemChanged(index)
    }

    fun notifyItemRemoved(index: Int) {
        feedRecyclerView.notifyItemRemoved(index)
        if(feedRecyclerView.isFeedListEmpty()){
            viewNoActivity.visibility = View.VISIBLE
        }
    }

    fun showViewNoFeeds(show: Boolean){
        viewNoActivity.visibility = if (show) View.VISIBLE else View.GONE
    }

}