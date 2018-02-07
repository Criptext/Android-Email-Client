package com.email.scenes.mailbox

import android.app.Activity
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.*
import android.widget.ImageView
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.androidui.ActivityMenu
import com.email.androidui.mailthread.ThreadListView
import com.email.androidui.mailthread.ThreadRecyclerView
import com.email.utils.Utility
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by sebas on 1/23/18.
 */

interface MailboxScene : ThreadListView{

    fun addToolbar()
    fun initDrawerLayout()
    fun initNavHeader()
    fun onBackPressed(activity: Activity)
    fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener)
    fun refreshToolbarItems()
    fun showMultiModeBar(selectedThreadsQuantity : Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle()
    fun tintIconsInMenu(activityMenu: ActivityMenu)

    class MailboxSceneView(private val sceneContainer: ViewGroup,
                           private val mailboxView: View,
                           val hostActivity: IHostActivity,
                           val threadListHandler: MailboxActivity.ThreadListHandler)
        : MailboxScene {

        private val context = mailboxView.context

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById<RecyclerView>(R.id.mailbox_recycler) as RecyclerView
        }

        val toolbar: Toolbar by lazy {
            mailboxView.findViewById<Toolbar>(R.id.mailbox_toolbar)
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

        private val avatarView: CircleImageView by lazy {
            mailboxView.findViewById<CircleImageView>(R.id.circleView)
        }

        private lateinit var threadRecyclerView: ThreadRecyclerView

        var threadListener: EmailThreadAdapter.OnThreadEventListener? = null
            set(value) {
                threadRecyclerView.setThreadListener(value)
                field = value
            }

        override fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener) {
            threadRecyclerView = ThreadRecyclerView(recyclerView, threadEventListener, threadListHandler)
            this.threadListener = threadEventListener

            sceneContainer.removeAllViews()
            sceneContainer.addView(mailboxView)
        }

        override fun initDrawerLayout() {
            navButton.setOnClickListener({
                drawerLayout.openDrawer(GravityCompat.START)
            })
        }

        override fun onBackPressed(activity: Activity) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                drawerLayout.closeDrawer(Gravity.LEFT)
            }
            else if (drawerLayout.isDrawerOpen(GravityCompat.END)){
                drawerLayout.closeDrawer(Gravity.RIGHT)
            }
            else{
                activity.finish()
            }
        }

        override fun initNavHeader() {
            avatarView.setImageBitmap(Utility.getBitmapFromText("Daniel Tigse Palma", "D", 250, 250))
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

        override fun addToolbar() {
            hostActivity.addToolbar(toolbar)
        }

        override fun showMultiModeBar(selectedThreadsQuantity : Int) {
            hostActivity.showMultiModeBar(selectedThreadsQuantity)
        }

        override fun hideMultiModeBar() {
            hostActivity.hideMultiModeBar()
        }

        override fun updateToolbarTitle() {
            hostActivity.updateToolbarTitle()
        }

        override fun tintIconsInMenu(activityMenu: ActivityMenu) {
            val deleteItem = activityMenu.findItemById(R.id.mailbox_delete_selected_messages)
            val archiveItem = activityMenu.findItemById(R.id.mailbox_archive_selected_messages)
            val toggleReadItem = activityMenu.findItemById(R.id.mailbox_toggle_read_selected_messages)
            Utility.addTintInMultiMode(context = this.context,
                    item = deleteItem)
            Utility.addTintInMultiMode(context = this.context,
                    item = archiveItem)
            Utility.addTintInMultiMode(context = this.context,
                    item = toggleReadItem)
        }
    }

}
