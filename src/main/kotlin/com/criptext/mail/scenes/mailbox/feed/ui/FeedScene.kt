package com.criptext.mail.scenes.mailbox.feed.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.scenes.mailbox.feed.VirtualFeedList
import com.criptext.mail.scenes.mailbox.feed.data.ActivityFeedItem
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import android.graphics.Rect
import android.graphics.Canvas
import android.graphics.Paint
import com.criptext.mail.utils.ui.ItemDecorator


/**
 * Created by danieltigse on 2/7/18.
 */

interface FeedScene {

    var feedEventListener: FeedItemHolder.FeedEventListener?
    var virtualListView: VirtualListView?
    var menu: IHostActivity.IActivityMenu?
    fun showError(errorMessage: String)
    fun setAdapter(virtualFeedList: VirtualFeedList, lastTimeFeedOpened: Long, totalNewFeeds: Int,
                   feedEventListener: FeedItemHolder.FeedEventListener)
    fun updateFeedBadge(totalNewFeeds: Int)
    fun initializeMenu(menu: IHostActivity.IActivityMenu)
    fun showStartGuideNotification()

    class Default(container: View): FeedScene {

        private val recyclerViewFeed: RecyclerView by lazy {
            container.findViewById<RecyclerView>(R.id.recyclerViewFeed)
        }

        private val context = container.context

        private var itemDecorator: ItemDecorator? = null

        override var virtualListView: VirtualListView? = null

        override var feedEventListener: FeedItemHolder.FeedEventListener? = null

        override var menu: IHostActivity.IActivityMenu? = null

        override fun setAdapter(virtualFeedList: VirtualFeedList, lastTimeFeedOpened: Long, totalNewFeeds: Int,
                                feedEventListener: FeedItemHolder.FeedEventListener) {

            this.feedEventListener = feedEventListener
            virtualListView = VirtualRecyclerView(recyclerViewFeed)
            val adapter = FeedItemAdapter(
                    feedItemsList = virtualFeedList,
                    lastTimeFeedOpened = lastTimeFeedOpened,
                    listener = feedEventListener)
            virtualListView?.setAdapter(adapter)
            addItemDecoratorRecyclerView(totalNewFeeds)

        }

        private fun addItemDecoratorRecyclerView(totalNewFeeds: Int){
            if(itemDecorator != null){
                recyclerViewFeed.removeItemDecoration(itemDecorator!!)
            }
            if(totalNewFeeds > 0){
                itemDecorator = ItemDecorator(recyclerViewFeed.context, totalNewFeeds)
                recyclerViewFeed.addItemDecoration(itemDecorator!!)
            }
        }

        override fun showError(errorMessage: String) {
            Toast.makeText(recyclerViewFeed.context, errorMessage, Toast.LENGTH_LONG).show()
        }

        override fun initializeMenu(menu: IHostActivity.IActivityMenu) {
            this.menu = menu
        }

        override fun updateFeedBadge(totalNewFeeds: Int) {
            val menuItem = menu?.findItemById(R.id.mailbox_bell_container)
            val actionView = menuItem?.actionView
            if(actionView != null && actionView is FrameLayout) {
                actionView.findViewById<FrameLayout>(R.id.view_alert_red_circle)?.visibility = if (totalNewFeeds > 0) View.VISIBLE else View.GONE
                actionView.findViewById<TextView>(R.id.view_alert_count_textview)?.text = totalNewFeeds.toString()
            }
        }

        override fun showStartGuideNotification() {
            val menuItem = menu?.findItemById(R.id.mailbox_bell_container)
            val actionView = menuItem?.actionView
            if(actionView != null && actionView is FrameLayout) {
                feedEventListener?.showStartGuideNotification(actionView)
            }
        }
    }

}