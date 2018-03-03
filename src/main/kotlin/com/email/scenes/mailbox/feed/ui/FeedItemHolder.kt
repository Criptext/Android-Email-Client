package com.email.scenes.mailbox.feed.ui

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.email.R
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.squareup.picasso.Picasso

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedItemHolder(view: View) : RecyclerView.ViewHolder(view), SwipeRevealLayout.SwipeListener{

    private val swipeView: SwipeRevealLayout
    private val containerView: LinearLayout
    private val imageViewTypeFeed: ImageView
    private val imageViewMute: ImageView
    private val imageViewMuteButton: ImageView
    private val textViewMute: TextView
    private val textViewTitle: TextView
    private val textViewDetail: TextView
    private val textViewDate: TextView
    private val viewMute: View
    private val viewDelete: View
    private val viewDate: View

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

    fun bindFeed(activityFeedItem: ActivityFeedItem, position: Int, listener: FeedClickListener?) {

        textViewTitle.text = activityFeedItem.title
        textViewDetail.text = activityFeedItem.subtitle

        checkIsNew(activityFeedItem)
        checkFeedType(activityFeedItem)
        checkIsMuted(activityFeedItem)
        setListeners(activityFeedItem, position, listener)
    }

    private fun checkIsNew(activityFeedItem: ActivityFeedItem){

        if(activityFeedItem.isNew){
            containerView.setBackgroundColor(ContextCompat.getColor(containerView.context,
                    R.color.menu_selected))
        }
        else{
            containerView.setBackgroundColor(Color.WHITE)
        }
    }

    private fun checkFeedType(activityFeedItem: ActivityFeedItem){

        if(activityFeedItem.type == ActivityFeedItem.FeedItemTypes.Mail.ordinal){
            Picasso.with(swipeView.context).load(R.drawable.read).into(imageViewTypeFeed)
        }
        else if(activityFeedItem.type == ActivityFeedItem.FeedItemTypes.File.ordinal){
            Picasso.with(swipeView.context).load(R.drawable.attachment).into(imageViewTypeFeed)
        }
    }

    private fun checkIsMuted(activityFeedItem: ActivityFeedItem){

        if(activityFeedItem.isMuted) {
            imageViewMute.visibility = View.VISIBLE
            Picasso.with(imageViewMuteButton.context).load(R.drawable.activity_feed).into(imageViewMuteButton)
            textViewMute.text = textViewMute.resources.getText(R.string.unmute)
        }
        else{
            imageViewMute.visibility = View.INVISIBLE
            Picasso.with(imageViewMuteButton.context).load(R.drawable.mute).into(imageViewMuteButton)
            textViewMute.text = textViewMute.resources.getText(R.string.mute)
        }
    }

    private fun setListeners(activityFeedItem: ActivityFeedItem, position: Int, listener: FeedClickListener?){

        viewMute.setOnClickListener {
            listener?.onMuteFeedItemClicked(activityFeedItem.id!!, position, !activityFeedItem.isMuted)
            swipeView.close(true)
        }

        viewDelete.setOnClickListener {
            listener?.onDeleteFeedItemClicked(activityFeedItem.id!!, position)
            swipeView.close(true)
        }

        viewDate.setOnClickListener({
            swipeView.close(true)
        })

        swipeView.setSwipeListener(this)
    }

    fun getSwipeView(): SwipeRevealLayout{
        return swipeView
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
        fun onMuteFeedItemClicked(feedId: Int, position: Int, isMuted: Boolean)
        fun onDeleteFeedItemClicked(feedId: Int, position: Int)
    }

}