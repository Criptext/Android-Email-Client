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
import com.email.db.FeedType
import com.email.db.models.Email
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.utils.DateUtils
import com.squareup.picasso.Picasso
import uk.co.chrisjenx.calligraphy.TypefaceUtils

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

    fun bindFeed(lastTimeFeedOpened: Long, activityFeedItem: ActivityFeedItem,
                 position: Int, listener: FeedEventListener?) {

        textViewTitle.text = "${activityFeedItem.contactName} ${getTitle(activityFeedItem.type)}"
        textViewDetail.text = getSubtitle(activityFeedItem)
        textViewDate.text = DateUtils.getFormattedDate(activityFeedItem.date.time)

        checkIsNew(lastTimeFeedOpened, position, activityFeedItem)
        checkFeedType(activityFeedItem)
        checkIsMuted(activityFeedItem)
        setListeners(activityFeedItem, position, listener)

    }

    private fun getSubtitle(activityFeedItem: ActivityFeedItem): String{
        return when(activityFeedItem.type){
            FeedType.OPEN_EMAIL -> activityFeedItem.emailSubject
            else -> activityFeedItem.fileName
        }
    }

    private fun getTitle(feedType: FeedType): String{
        return when(feedType){
            FeedType.DOWNLOAD_FILE -> containerView.context.resources.getString(R.string.feed_downloaded)
            else -> containerView.context.resources.getString(R.string.feed_opened)
        }
    }

    private fun checkIsNew(lastTimeFeedOpened: Long, position: Int,
                           activityFeedItem: ActivityFeedItem){

        if(activityFeedItem.date.time > lastTimeFeedOpened){
            setViewAsNew()
        }
        else{
            setViewAsOlder()
        }

    }

    private fun setViewAsNew(){
        containerView.setBackgroundColor(ContextCompat.getColor(containerView.context,
                R.color.menu_selected))
        textViewDate.typeface = TypefaceUtils.load(textViewDate.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
        textViewTitle.typeface = TypefaceUtils.load(textViewTitle.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
    }

    private fun setViewAsOlder(){
        containerView.setBackgroundColor(Color.WHITE)
        textViewDate.typeface = TypefaceUtils.load(textViewDate.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitle.typeface = TypefaceUtils.load(textViewTitle.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
    }

    private fun checkFeedType(activityFeedItem: ActivityFeedItem){

        when(activityFeedItem.type){
            FeedType.OPEN_EMAIL -> Picasso.with(swipeView.context).load(R.drawable.read).into(imageViewTypeFeed)
            else -> Picasso.with(swipeView.context).load(R.drawable.attachment).into(imageViewTypeFeed)
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

    private fun setListeners(activityFeedItem: ActivityFeedItem, position: Int, listener: FeedEventListener?){

        viewMute.setOnClickListener {
            listener?.onMuteFeedItemClicked(activityFeedItem.id, position, !activityFeedItem.isMuted)
            swipeView.close(true)
        }

        viewDelete.setOnClickListener {
            listener?.onDeleteFeedItemClicked(activityFeedItem.id, position)
            swipeView.close(true)
        }

        viewDate.setOnClickListener({
            swipeView.close(true)
        })

        containerView.setOnClickListener {
            containerView.setBackgroundColor(Color.WHITE)
            listener?.onFeedItemClicked(activityFeedItem.email)
        }

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

    interface FeedEventListener{
        fun onFeedItemClicked(email: Email)
        fun onMuteFeedItemClicked(feedId: Long, position: Int, isMuted: Boolean)
        fun onDeleteFeedItemClicked(feedId: Long, position: Int)
        fun onApproachingEnd()
    }

}