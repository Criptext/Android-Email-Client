package com.email.scenes.mailbox

import android.support.design.widget.NavigationView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedHolder
import com.email.scenes.mailbox.ui.FeedRecyclerView
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by danieltigse on 2/7/18.
 */

class DrawerFeedView(navigationView: NavigationView, feedClickListener: FeedHolder.FeedClickListener){

    val viewNoActivity : LinearLayout
    val recyclerViewFeed: RecyclerView

    private var feedRecyclerView: FeedRecyclerView

    init {
        viewNoActivity = navigationView.findViewById(R.id.viewNoActivity)
        recyclerViewFeed = navigationView.findViewById(R.id.recyclerViewFeed)

        feedRecyclerView = FeedRecyclerView(recyclerViewFeed, feedClickListener)
    }

    fun setFeedList(feeds: List<ActivityFeed>){
        feedRecyclerView.setFeedList(feeds)
        viewNoActivity.visibility = View.GONE
    }

}