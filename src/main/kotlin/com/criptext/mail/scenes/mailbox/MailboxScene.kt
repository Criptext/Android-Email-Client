package com.criptext.mail.scenes.mailbox

import android.app.Activity
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
import android.widget.RelativeLayout
import android.widget.TextView
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.utils.virtuallist.VirtualListView
import com.criptext.mail.utils.virtuallist.VirtualRecyclerView
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.LabelChooserDialog
import com.criptext.mail.scenes.label_chooser.LabelDataHandler
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.holders.ToolbarHolder
import com.criptext.mail.scenes.mailbox.ui.DrawerMenuView
import com.criptext.mail.scenes.mailbox.ui.EmailThreadAdapter
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver
import com.criptext.mail.scenes.mailbox.ui.WelcomeTour.WelcomeTourDialog
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import com.criptext.mail.utils.ui.*
import com.criptext.mail.utils.uiobserver.UIObserver
import kotlinx.android.synthetic.main.mail_item_left_name.view.*

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
            threadEventListener: EmailThreadAdapter.OnThreadEventListener,
            onDrawerMenuItemListener: DrawerMenuItemListener,
            observer: MailboxUIObserver,
            threadList: VirtualEmailThreadList)
    fun refreshToolbarItems()
    fun showMultiModeBar(selectedThreadsQuantity : Int)
    fun hideMultiModeBar()
    fun updateToolbarTitle(title: String)
    fun showDialogLabelsChooser(labelDataSourceHandler: LabelDataHandler)
    fun showDialogMoveTo(onMoveThreadsListener: OnMoveThreadsListener, currentFolder: String)
    fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener)
    fun showWelcomeDialog(observer: MailboxUIObserver)
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
    fun setMenuLabels(labels: List<LabelWrapper>)
    fun clearMenuActiveLabel()
    fun dismissConfirmPasswordDialog()
    fun showConfirmPasswordDialog(observer: UIObserver)
    fun setConfirmPasswordError(message: UIMessage)
    fun showEmptyTrashBanner()
    fun hideEmptyTrashBanner()
    fun showEmptyTrashWarningDialog(onEmptyTrashListener: OnEmptyTrashListener)
    fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun showSyncPhonebookDialog(observer: MailboxUIObserver)
    fun showStartGuideEmail()
    fun showStartGuideMultiple()

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
        private val emptyTrashDialog = EmptyTrashDialog(context)
        private val welcomeDialog = WelcomeTourDialog(context)
        private val confirmPassword = ConfirmPasswordDialog(context)
        private val linkAuthDialog = LinkNewDeviceAlertDialog(context)
        private val syncPhonebookDialog = SyncPhonebookDialog(context)

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

        private val trashBanner: RelativeLayout by lazy {
            mailboxView.findViewById<RelativeLayout>(R.id.empty_trash_banner)
        }

        private val emptyTrashButton: TextView by lazy {
            mailboxView.findViewById<TextView>(R.id.empty_trash)
        }

        override val virtualListView = VirtualRecyclerView(recyclerView)


        override fun attachView(
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
            navButton.setOnClickListener{
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        override fun showLinkDeviceAuthConfirmation(untrustedDeviceInfo: UntrustedDeviceInfo) {
            if(linkAuthDialog.isShowing() != null && linkAuthDialog.isShowing() == false)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
            else if(linkAuthDialog.isShowing() == null)
                linkAuthDialog.showLinkDeviceAuthDialog(observer, untrustedDeviceInfo)
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
                                      currentFolder: String) {
            this.onMoveThreadsListener = onMoveThreadsListener
            moveToDialog.showMoveToDialog(
                    onMoveThreadsListener = onMoveThreadsListener,
                    currentFolder = currentFolder)
        }

        override fun showDialogDeleteThread(onDeleteThreadListener: OnDeleteThreadListener) {
            deleteDialog.showDeleteThreadDialog(onDeleteThreadListener)
        }

        override fun showSyncPhonebookDialog(observer: MailboxUIObserver) {
            syncPhonebookDialog.showSyncPhonebookDialog(observer)
        }

        override fun showStartGuideEmail() {
            observer?.showStartGuideEmail(openComposerButton)
        }

        override fun showStartGuideMultiple(){
            if(recyclerView.adapter.itemCount > 2) {
                val view = recyclerView.findViewHolderForAdapterPosition(0).itemView.mail_item_left_name
                observer?.showStartGuideMultiple(view)
            }
        }

        override fun showEmptyTrashWarningDialog(onEmptyTrashListener: OnEmptyTrashListener) {
            emptyTrashDialog.showEmptyTrashDialog(onEmptyTrashListener)
        }

        override fun showWelcomeDialog(observer: MailboxUIObserver) {
            welcomeDialog.showWelcomeTourDialog(observer)
        }

        override fun dismissConfirmPasswordDialog() {
            confirmPassword.dismissDialog()
        }

        override fun setConfirmPasswordError(message: UIMessage) {
            confirmPassword.setPasswordError(message)
        }

        override fun showConfirmPasswordDialog(observer: UIObserver) {
            confirmPassword.showDialog(observer)
        }

        override fun setToolbarNumberOfEmails(emailsSize: Int) {
            toolbarHolder.updateNumberOfMails(emailsSize)
        }

        override fun hideEmptyTrashBanner() {
            trashBanner.visibility = View.GONE
        }

        override fun showEmptyTrashBanner() {
            trashBanner.visibility = View.VISIBLE
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

            emptyTrashButton.setOnClickListener{
                observer.onEmptyTrashPressed()
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

        override fun setMenuLabels(labels: List<LabelWrapper>) {
            drawerMenuView.setLabelAdapter(labels)
        }

        override fun clearMenuActiveLabel() {
            drawerMenuView.clearActiveLabel()
        }

    }
}
