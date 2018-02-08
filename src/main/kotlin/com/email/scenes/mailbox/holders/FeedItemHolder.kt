package com.email.scenes.mailbox.holders

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.email.R
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.utils.Utility
import com.squareup.picasso.Picasso

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedItemHolder(view: View) : RecyclerView.ViewHolder(view), SwipeRevealLayout.SwipeListener{

    val swipeView: SwipeRevealLayout
    val containerView: LinearLayout
    val imageViewTypeFeed: ImageView
    val imageViewMute: ImageView
    val imageViewMuteButton: ImageView
    val textViewMute: TextView
    val textViewTitle: TextView
    val textViewDetail: TextView
    val textViewDate: TextView
    val viewMute: View
    val viewDelete: View
    val viewDate: View

    init {

        swipeView = view.findViewById(R.id.swipeView)
        containerView = view.findViewById(R.id.containerView)
        viewDelete = view.findViewById(R.id.viewDelete)
        viewMute = view.findViewById(R.id.viewMute)
        viewDate = view.findViewById(R.id.viewDate)
        imageViewTypeFeed = view.findViewById(R.id.imageViewTypeFeed)
        imageViewMute = view.findViewById(R.id.imageViewMute)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        imageViewMuteButton = view.findViewById(R.id.imageViewMuteButton)
        textViewMute = view.findViewById(R.id.textViewMute)
        textViewDetail = view.findViewById(R.id.textViewDetail)
        textViewDate = view.findViewById(R.id.textViewDate)

    }

    fun bindFeed(activityFeed: ActivityFeed, listener: FeedClickListener) {

        if(activityFeed.isNew){
            containerView.setBackgroundColor(swipeView.resources.getColor(R.color.menu_selected))
        }
        else{
            containerView.setBackgroundColor(Color.WHITE)
        }

        if(activityFeed.feedType == ActivityFeed.FeedItemTypes.Mail.ordinal){
            Picasso.with(swipeView.context).load(R.drawable.read).into(imageViewTypeFeed)
        }
        else if(activityFeed.feedType == ActivityFeed.FeedItemTypes.File.ordinal){
            Picasso.with(swipeView.context).load(R.drawable.attachment).into(imageViewTypeFeed)
        }

        if(activityFeed.isMuted) {
            imageViewMute.visibility = View.VISIBLE
            Picasso.with(imageViewMuteButton.context).load(R.drawable.activity_feed).into(imageViewMuteButton)
            textViewMute.text = textViewMute.resources.getText(R.string.unmute)
        }
        else{
            imageViewMute.visibility = View.GONE
            Picasso.with(imageViewMuteButton.context).load(R.drawable.mute).into(imageViewMuteButton)
            textViewMute.text = textViewMute.resources.getText(R.string.mute)
        }

        textViewTitle.text = activityFeed.feedTitle
        textViewDetail.text = activityFeed.feedSubtitle

        viewMute.setOnClickListener {
            activityFeed.isMuted = !activityFeed.isMuted
            listener.onFeedMuted(activityFeed)
            swipeView.close(true)
        }

        viewDelete.setOnClickListener {
            listener.onFeedDeleted(activityFeed)
            swipeView.close(true)
        }

        viewDate.setOnClickListener({
            swipeView.close(true)
        })

        swipeView.setSwipeListener(this)
    }

    override fun onOpened(view: SwipeRevealLayout?) {
        swipeView.setLockDrag(true)
    }

    override fun onClosed(view: SwipeRevealLayout?) {
        swipeView.setLockDrag(false)
    }

    override fun onSlide(view: SwipeRevealLayout?, slideOffset: Float) {

    }

    interface FeedClickListener{
        fun onFeedMuted(activityFeed: ActivityFeed)
        fun onFeedDeleted(activityFeed: ActivityFeed)
    }

}