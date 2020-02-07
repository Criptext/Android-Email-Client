package com.criptext.mail.scenes.mailbox.feed.ui


import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.criptext.mail.R
import com.criptext.mail.db.FeedType
import com.criptext.mail.db.models.Email
import com.criptext.mail.scenes.mailbox.feed.data.ActivityFeedItem
import com.criptext.mail.utils.DateAndTimeUtils
import com.criptext.mail.utils.getColorFromAttr
import com.squareup.picasso.Picasso
import uk.co.chrisjenx.calligraphy.TypefaceUtils

/**
 * Created by danieltigse on 2/7/18.
 */

class FeedItemHolder(private val view: View) : RecyclerView.ViewHolder(view), SwipeRevealLayout.SwipeListener{

    private val swipeView: SwipeRevealLayout
    private val containerView: LinearLayout
    private val imageViewTypeFeed: ImageView
    private val imageViewMute: ImageView
    private val textViewTitle: TextView
    private val textViewDetail: TextView
    private val textViewDate: TextView
    private val viewDelete: View
    private val viewDate: View

    init {

        swipeView = view.findViewById(R.id.swipeView)
        containerView = view.findViewById(R.id.containerView)
        viewDelete = view.findViewById(R.id.viewDelete)
        viewDate = view.findViewById(R.id.viewDate)
        imageViewTypeFeed = view.findViewById(R.id.imageViewTypeFeed)
        imageViewMute = view.findViewById(R.id.imageViewMute)
        textViewTitle = view.findViewById(R.id.textViewTitle)
        textViewDetail = view.findViewById(R.id.textViewDetail)
        textViewDate = view.findViewById(R.id.textViewDate)

    }

    fun bindFeed(lastTimeFeedOpened: Long, activityFeedItem: ActivityFeedItem,
                 position: Int, listener: FeedEventListener?) {

        textViewTitle.text = "${activityFeedItem.contactName} ${getTitle(activityFeedItem.type)}"
        textViewDetail.text = getSubtitle(activityFeedItem)
        textViewDate.text = DateAndTimeUtils.getFormattedDate(activityFeedItem.date.time, view.context)

        checkIsNew(lastTimeFeedOpened, activityFeedItem)
        checkFeedType(activityFeedItem)
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

    private fun checkIsNew(lastTimeFeedOpened: Long,
                           activityFeedItem: ActivityFeedItem){

        if(activityFeedItem.date.time > lastTimeFeedOpened){
            setViewAsNew()
        }
        else{
            setViewAsOlder()
        }

    }

    private fun setViewAsNew(){
        containerView.setBackgroundColor(containerView.context.getColorFromAttr(R.attr.criptextLeftMenuSelected))
        textViewDate.typeface = TypefaceUtils.load(textViewDate.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
        textViewTitle.typeface = TypefaceUtils.load(textViewTitle.resources.assets,
                "fonts/NunitoSans-Bold.ttf")
    }

    private fun setViewAsOlder(){
        containerView.setBackgroundColor(containerView.context.getColorFromAttr(R.attr.criptextColorBackground))
        textViewDate.typeface = TypefaceUtils.load(textViewDate.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
        textViewTitle.typeface = TypefaceUtils.load(textViewTitle.resources.assets,
                "fonts/NunitoSans-Regular.ttf")
    }

    private fun checkFeedType(activityFeedItem: ActivityFeedItem){

        when(activityFeedItem.type){
            FeedType.OPEN_EMAIL -> Picasso.get().load(R.drawable.read).into(imageViewTypeFeed)
            else -> Picasso.get().load(R.drawable.attachment).into(imageViewTypeFeed)
        }
    }

    private fun setListeners(activityFeedItem: ActivityFeedItem, position: Int, listener: FeedEventListener?){

        viewDelete.setOnClickListener {
            listener?.onDeleteFeedItemClicked(activityFeedItem.id, position)
            swipeView.close(true)
        }

        viewDate.setOnClickListener {
            swipeView.close(true)
        }

        containerView.setOnClickListener {
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
        fun onDeleteFeedItemClicked(feedId: Long, position: Int)
        fun onApproachingEnd()
        fun showStartGuideNotification(view: View)
    }

}