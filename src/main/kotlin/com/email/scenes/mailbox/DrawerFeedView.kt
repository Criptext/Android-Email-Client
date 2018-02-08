package com.email.scenes.mailbox

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedItemHolder
import com.email.scenes.mailbox.ui.FeedRecyclerView

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerFeedView(navigationView: NavigationView, feedClickListener: FeedItemHolder.FeedClickListener){

    private val viewNoActivity : LinearLayout
    private val recyclerViewFeed: RecyclerView

    private var feedRecyclerView: FeedRecyclerView

    init {
        viewNoActivity = navigationView.findViewById(R.id.viewNoActivity)
        recyclerViewFeed = navigationView.findViewById(R.id.recyclerViewFeed)

        feedRecyclerView = FeedRecyclerView(recyclerViewFeed, feedClickListener)
    }

    fun setFeedList(feeds: MutableList<ActivityFeed>){
        feedRecyclerView.setFeedList(feeds)
        viewNoActivity.visibility = View.GONE
    }

    fun notifyItemChanged(activityFeed: ActivityFeed) {
        feedRecyclerView.notifyItemChanged(activityFeed)
    }

    fun notifyItemRemoved(activityFeed: ActivityFeed) {
        feedRecyclerView.notifyItemRemoved(activityFeed)
        if(feedRecyclerView.isFeedListEmpty()){
            viewNoActivity.visibility = View.VISIBLE
        }
    }

}