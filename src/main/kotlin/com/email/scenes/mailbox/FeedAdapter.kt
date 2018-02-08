package com.email.scenes.mailbox

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.holders.FeedHolder

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedAdapter(private val listener: FeedHolder.FeedClickListener): RecyclerView.Adapter<FeedHolder>() {

    lateinit var feeds : List<ActivityFeed>

    override fun onBindViewHolder(holder: FeedHolder?, position: Int) {
        val feed = feeds[position]
        holder?.bindFeed(feed, listener)
    }

    override fun getItemCount(): Int {
        return feeds.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedHolder(inflatedView)
    }

}
