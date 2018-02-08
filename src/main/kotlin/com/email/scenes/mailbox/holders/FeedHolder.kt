package com.email.scenes.mailbox.holders

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.utils.Utility
import com.squareup.picasso.Picasso
import me.thanel.swipeactionview.SwipeActionView
import me.thanel.swipeactionview.SwipeDirection
import me.thanel.swipeactionview.SwipeGestureListener

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

    val swipeView: SwipeActionView
    val containerView: LinearLayout
    val imageViewTypeFeed: ImageView
    val imageViewMute: ImageView
    val textViewTitle: TextView
    val textViewDetail: TextView
    val textViewDate: TextView
    val viewMute: View
    val viewDelete: View

    init {

        swipeView = view.findViewById(R.id.swipeView)
        containerView = view.findViewById(R.id.containerView)
        viewDelete = view.findViewById(R.id.viewDelete)
        viewMute = view.findViewById(R.id.viewMute)
        imageViewTypeFeed = view.findViewById(R.id.imageViewTypeFeed)
        imageViewMute = view.findViewById(R.id.imageViewMute)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewDetail = view.findViewById(R.id.textViewDetail)
        textViewDate = view.findViewById(R.id.textViewDate)
        view.setOnClickListener(this)

        swipeView.setDirectionEnabled(SwipeDirection.Left, true)
        swipeView.setDirectionEnabled(SwipeDirection.Right, false)
        swipeView.swipeGestureListener = object : SwipeGestureListener {
            override fun onSwipedLeft(swipeActionView: SwipeActionView): Boolean {
                return false
            }

            override fun onSwipedRight(swipeActionView: SwipeActionView): Boolean {
                return false
            }
        }
    }

    fun bindFeed(activityFeed: ActivityFeed, listener: FeedClickListener) {

        if(activityFeed.isNew){
            containerView.setBackgroundColor(swipeView.resources.getColor(R.color.menu_selected))
        }
        else{
            containerView.setBackgroundColor(Color.WHITE)
        }

        if(activityFeed.feedType == Utility.FEED_MAIL){
            Picasso.with(swipeView.context).load(R.drawable.read).into(imageViewTypeFeed)
        }
        else if(activityFeed.feedType == Utility.FEED_FILE){
            Picasso.with(swipeView.context).load(R.drawable.attachment).into(imageViewTypeFeed)
        }

        if(activityFeed.isMuted) {
            imageViewMute.visibility = View.VISIBLE
        }
        else{
            imageViewMute.visibility = View.GONE
        }

        textViewTitle.text = activityFeed.feedTitle
        textViewDetail.text = activityFeed.feedSubtitle

        viewMute.setOnClickListener {
            listener.onFeedMuted(activityFeed)
        }

        viewDelete.setOnClickListener {
            listener.onFeedDeleted(activityFeed)
        }
    }

    override fun onClick(p0: View?) {
    }

    interface FeedClickListener{
        fun onFeedMuted(activityFeed: ActivityFeed)
        fun onFeedDeleted(activityFeed: ActivityFeed)
    }

}