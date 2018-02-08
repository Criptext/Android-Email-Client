package com.email.scenes.mailbox

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.*
import android.widget.ImageView
import com.email.IHostActivity
import com.email.R
import com.email.androidui.mailthread.ThreadListView
import com.email.androidui.mailthread.ThreadRecyclerView
import com.email.scenes.LabelChooser.LabelChooserDialog
import com.email.scenes.LabelChooser.LabelDataSourceHandler
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.holders.ToolbarHolder
import com.email.scenes.mailbox.holders.FeedHolder
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/23/18.
 */

interface MailboxScene: ThreadListView {

    fun setFeedList(feeds: List<ActivityFeed>)
    fun initDrawerLayout()
    fun initNavHeader(fullName: String)
    fun onBackPressed()
    fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener, feedClickListener: FeedHolder.FeedClickListener)
    fun refreshToolbarItems()
    fun showMultiModeBar(selectedThreadsQuantity : Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle(title: String)
    fun showDialogLabelsChooser(labelDataSourceHandler: LabelDataSourceHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener)

    class MailboxSceneView(private val mailboxView: View,
                           val hostActivity: IHostActivity,
                           val threadList: VirtualList<EmailThread>)
        : MailboxScene {

        private val context = mailboxView.context

        private val labelChooserDialog = LabelChooserDialog(context)
        private val moveToDialog = MoveToDialog(context)

        lateinit var drawerMenuView: DrawerMenuView
        lateinit var drawerFeedView: DrawerFeedView

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById<RecyclerView>(R.id.mailbox_recycler)
        }

        val toolbarHolder: ToolbarHolder by lazy {
            val view = mailboxView.findViewById<Toolbar>(R.id.mailbox_toolbar)
            ToolbarHolder(view)
        }

        private val drawerLayout: DrawerLayout by lazy {
            mailboxView.findViewById<DrawerLayout>(R.id.drawer_layout) as DrawerLayout
        }

        private val leftNavigationView: NavigationView by lazy {
            mailboxView.findViewById<NavigationView>(R.id.nav_left_view)
        }

        private val rightNavigationView: NavigationView by lazy {
            mailboxView.findViewById<NavigationView>(R.id.nav_right_view)
        }

        private val navButton: ImageView by lazy {
            mailboxView.findViewById<ImageView>(R.id.mailbox_nav_button)
        }

        private lateinit var threadRecyclerView: ThreadRecyclerView

        var threadListener: EmailThreadAdapter.OnThreadEventListener? = null
            set(value) {
                threadRecyclerView.setThreadListener(value)
                field = value
            }

        override fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener,
                                feedClickListener: FeedHolder.FeedClickListener) {
            threadRecyclerView = ThreadRecyclerView(recyclerView, threadEventListener, threadList)
            this.threadListener = threadEventListener

            drawerMenuView = DrawerMenuView(leftNavigationView)
            drawerFeedView = DrawerFeedView(rightNavigationView, feedClickListener)
        }

        override fun initDrawerLayout() {
            navButton.setOnClickListener({
                drawerLayout.openDrawer(GravityCompat.START)
            })
        }

        override fun onBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            else if (drawerLayout.isDrawerOpen(GravityCompat.END)){
                drawerLayout.closeDrawer(Gravity.RIGHT)
            }
        }

        override fun initNavHeader(fullName: String) {
            drawerMenuView.initNavHeader(fullName)
        }

        override fun setFeedList(feeds: List<ActivityFeed>) {
            drawerFeedView.setFeedList(feeds)
        }

        override fun notifyThreadSetChanged() {
            threadRecyclerView.notifyThreadSetChanged()
        }

        override fun notifyThreadRemoved(position: Int) {
            threadRecyclerView.notifyThreadRemoved(position)
        }

        override fun notifyThreadChanged(position: Int) {
            threadRecyclerView.notifyThreadChanged(position)
        }

        override fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
            threadRecyclerView.notifyThreadRangeInserted(positionStart, itemCount)
        }

        override fun changeMode(multiSelectON: Boolean, silent: Boolean) {
            threadRecyclerView.changeMode(multiSelectON, silent)
        }

        override fun refreshToolbarItems() {
            hostActivity.refreshToolbarItems()
        }

        override fun showMultiModeBar(selectedThreadsQuantity : Int) {
            toolbarHolder.showMultiModeBar(selectedThreadsQuantity)
        }

        override fun hideMultiModeBar() {
            toolbarHolder.hideMultiModeBar()
        }

        override fun updateToolbarTitle(title: String) {
            toolbarHolder.updateToolbarTitle(title)
        }

        override fun showDialogLabelsChooser( labelDataSourceHandler:
                                              LabelDataSourceHandler ) {
            labelChooserDialog.showDialogLabelsChooser(dataSource = labelDataSourceHandler)
        }

        override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener) {
            moveToDialog.showMoveToDialog(
                    moveToDataSourceHandler = onMoveThreadsListener)
        }

    }

}
