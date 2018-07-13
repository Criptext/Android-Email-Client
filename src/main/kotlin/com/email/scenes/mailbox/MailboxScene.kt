package com.email.scenes.mailbox

import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.*
import android.widget.ImageView
import com.email.IHostActivity
import com.email.R
import com.email.utils.virtuallist.VirtualListView
import com.email.utils.virtuallist.VirtualRecyclerView
import com.email.db.MailFolders
import com.email.db.models.Label
import com.email.scenes.label_chooser.LabelChooserDialog
import com.email.scenes.label_chooser.LabelDataHandler
import com.email.scenes.mailbox.holders.ToolbarHolder
import com.email.scenes.mailbox.ui.DrawerMenuView
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.ui.MailboxUIObserver
import com.email.utils.UIMessage
import com.email.utils.getLocalizedUIMessage
import com.email.utils.ui.SnackBarHelper

/**
 * Created by sebas on 1/23/18.
 */

interface MailboxScene{
    val virtualListView: VirtualListView
    var observer: MailboxUIObserver?
    var threadEventListener: EmailThreadAdapter.OnThreadEventListener?
    var onMoveThreadsListener: OnMoveThreadsListener?
    fun showSyncingDialog()
    fun hideSyncingDialog()
    fun initDrawerLayout()
    fun initNavHeader(fullName: String, email: String)
    fun onBackPressed(): Boolean
    fun attachView(
            mailboxLabel: MailFolders,
            threadEventListener: EmailThreadAdapter.OnThreadEventListener,
            onDrawerMenuItemListener: DrawerMenuItemListener,
            observer: MailboxUIObserver,
            threadList: VirtualEmailThreadList)
    fun refreshToolbarItems()
    fun showMultiModeBar(selectedThreadsQuantity : Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle(title: String)
    fun showDialogLabelsChooser(labelDataSourceHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener, currentFolder: MailFolders)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun setToolbarNumberOfEmails(emailsSize: Int)
    fun openNotificationFeed()
    fun onFetchedSelectedLabels(defaultSelectedLabels: List<Label>, labels: List<Label>)
    fun refreshMails()
    fun clearRefreshing()
    fun showMessage(message : UIMessage)
    fun hideDrawer()
    fun showRefresh()
    fun scrollTop()
    fun setCounterLabel(menu: NavigationMenuOptions, total: Int)

    class MailboxSceneView(private val mailboxView: View, val hostActivity: IHostActivity)
        : MailboxScene {
        override fun showSyncingDialog() {
            hostActivity.showDialog(UIMessage(R.string.updating_mailbox))
        }

        override fun hideSyncingDialog() {
            hostActivity.dismissDialog()
        }

        private val context = mailboxView.context

        private lateinit var adapter: EmailThreadAdapter

        override var observer: MailboxUIObserver? = null

        override var threadEventListener: EmailThreadAdapter.OnThreadEventListener? = null

        override var onMoveThreadsListener: OnMoveThreadsListener? = null

        private val labelChooserDialog = LabelChooserDialog(context, mailboxView)
        private val moveToDialog = MoveToDialog(context)
        private val deleteDialog = DeleteThreadDialog(context)

        private lateinit var drawerMenuView: DrawerMenuView

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById<RecyclerView>(R.id.mailbox_recycler)
        }

        private val refreshLayout: SwipeRefreshLayout by lazy {
            mailboxView.findViewById<SwipeRefreshLayout>(R.id.mailbox_refresher)
        }

        private val toolbarHolder: ToolbarHolder by lazy {
            val view = mailboxView.findViewById<Toolbar>(R.id.mailbox_toolbar)
            ToolbarHolder(view)
        }

        private val drawerLayout: DrawerLayout by lazy {
            mailboxView.findViewById<DrawerLayout>(R.id.drawer_layout)
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

        private val openComposerButton: View by lazy {
            mailboxView.findViewById<View>(R.id.fab)
        }

        private val backButton: ImageView by lazy {
            mailboxView.findViewById<ImageView>(R.id.mailbox_back_button)
        }

        override val virtualListView = VirtualRecyclerView(recyclerView)


        override fun attachView(
                mailboxLabel: MailFolders,
                threadEventListener: EmailThreadAdapter.OnThreadEventListener,
                onDrawerMenuItemListener: DrawerMenuItemListener,
                observer: MailboxUIObserver,
                threadList: VirtualEmailThreadList) {

            drawerMenuView = DrawerMenuView(leftNavigationView, onDrawerMenuItemListener)

            adapter = EmailThreadAdapter(threadListener = threadEventListener,
                                         threadList = threadList)
            virtualListView.setAdapter(adapter)
            this.observer = observer
            this.threadEventListener = threadEventListener
            setListeners(observer)

        }

        override fun initDrawerLayout() {
            navButton.setOnClickListener({
                drawerLayout.openDrawer(GravityCompat.START)
            })
        }

        override fun onBackPressed(): Boolean {
            return when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                    drawerLayout.closeDrawer(Gravity.LEFT)
                    false
                }
                drawerLayout.isDrawerOpen(GravityCompat.END) -> {
                    drawerLayout.closeDrawer(Gravity.RIGHT)
                    false
                }
                else -> true
            }
        }

        override fun initNavHeader(fullName: String, email: String) {
            drawerMenuView.initNavHeader(fullName, email)
        }

        override fun scrollTop() {
            recyclerView.smoothScrollToPosition(0)
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
                                              LabelDataHandler ) {
            labelChooserDialog.showDialogLabelsChooser(dataHandler = labelDataSourceHandler)
        }

        override fun openNotificationFeed(){
            drawerLayout.openDrawer(GravityCompat.END)
        }

        override fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener,
                                      currentFolder: MailFolders) {
            this.onMoveThreadsListener = onMoveThreadsListener
            moveToDialog.showMoveToDialog(
                    onMoveThreadsListener = onMoveThreadsListener,
                    currentFolder = currentFolder)
        }

        override fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener) {
            deleteDialog.showDeleteThreadDialog(onDeleteThreadListener)
        }

        override fun setToolbarNumberOfEmails(emailsSize: Int) {
            toolbarHolder.updateNumberOfMails(emailsSize)
        }

        override fun onFetchedSelectedLabels(defaultSelectedLabels: List<Label>, labels: List<Label>) {
            labelChooserDialog.onFetchedLabels(
                    defaultSelectedLabels = defaultSelectedLabels,
                    allLabels = labels)
        }

        override fun refreshMails() {
            if(refreshLayout.isRefreshing) {
                Handler().postDelayed({
                    refreshLayout.isRefreshing = false
                }, 1000)
            }
        }

        override fun showRefresh() {
            refreshLayout.isRefreshing = true
        }

        override fun clearRefreshing() {
            refreshLayout.isRefreshing = false
        }

        override fun showMessage(message: UIMessage) {
            SnackBarHelper.show(openComposerButton, context.getLocalizedUIMessage(message))
        }

        override fun hideDrawer() {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        override fun setCounterLabel(menu: NavigationMenuOptions, total: Int) {
            drawerMenuView.setCounterLabel(menu, total)
        }

        private fun setListeners(observer: MailboxUIObserver){

            openComposerButton.setOnClickListener {
                observer.onOpenComposerButtonClicked()
            }

            refreshLayout.setOnRefreshListener {
                observer.onRefreshMails()
            }

            backButton.setOnClickListener {
                observer.onBackButtonPressed()
            }

            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
                override fun onDrawerStateChanged(newState: Int) {}
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerClosed(drawerView: View) {
                    if(drawerView == rightNavigationView){
                        observer.onFeedDrawerClosed()
                    }
                }
                override fun onDrawerOpened(drawerView: View) {}
            })
        }

    }
}
