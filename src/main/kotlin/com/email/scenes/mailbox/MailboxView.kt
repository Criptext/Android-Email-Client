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
import com.email.scenes.LabelChooser.LabelChooserDialog
import com.email.scenes.LabelChooser.LabelDataSourceHandler
import com.email.utils.Utility
import com.email.utils.ui.Tint
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
    fun showDialogLabelsChooser(labelDataSourceHandler: LabelDataSourceHandler)
    fun tintIconsInMenu(activityMenu: ActivityMenu, multiSelectON: Boolean)
    fun getLabelDataSourceHandler(): LabelDataSourceHandler
    fun getOnMoveThreadsListener(): OnMoveThreadsListener
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener)

    class MailboxSceneView(private val sceneContainer: ViewGroup,
                           private val mailboxView: View,
                           val hostActivity: IHostActivity,
                           val threadListHandler: MailboxActivity.ThreadListHandler)
        : MailboxScene {

        private val context = mailboxView.context

        private val labelChooserDialog = LabelChooserDialog(context)
        private val moveToDialog = MoveToDialog(context)

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById(R.id.mailbox_recycler) as RecyclerView
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

        override fun tintIconsInMenu(activityMenu: ActivityMenu, multiSelectON: Boolean) {
            if(multiSelectON){
                val deleteItem = activityMenu.findItemById(R.id.mailbox_delete_selected_messages)
                val archiveItem = activityMenu.findItemById(R.id.mailbox_archive_selected_messages)
                val toggleReadItem = activityMenu.findItemById(R.id.mailbox_message_toggle_read)
                Tint.addTintToMenuItem(context = this.context,
                        item = deleteItem)
                Tint.addTintToMenuItem(context = this.context,
                        item = archiveItem)
                Tint.addTintToMenuItem(context = this.context,
                        item = toggleReadItem)
               Tint.addTintToMenuItem(context = this.context,
                        item = toggleReadItem)

            } else {
                val search = activityMenu.findItemById(R.id.mailbox_search)
                val bellContainer = activityMenu.findItemById(R.id.mailbox_bell_container)
                val bell = bellContainer.actionView.findViewById(R.id.mailbox_activity_feed) as ImageView
                Tint.addTintToMenuItem(context = this.context,
                        item = search)

                Tint.addTintToImage(context = this.context,
                        imageView = bell)
            }
        }

        override fun showDialogLabelsChooser( labelDataSourceHandler:
                                              LabelDataSourceHandler ) {
            labelChooserDialog.showdialogLabelsChooser(
                    labelDataSourceHandler = labelDataSourceHandler)
        }

        override fun getLabelDataSourceHandler(): LabelDataSourceHandler {
            return (hostActivity as MailboxActivity).labelDataSourceHandler
        }

        override fun getOnMoveThreadsListener(): OnMoveThreadsListener {
            return (hostActivity as MailboxActivity).onMoveThreadsListener
        }

        override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener) {
            moveToDialog.showMoveToDialog(
                    moveToDataSourceHandler = onMoveThreadsListener)
        }
    }
}
