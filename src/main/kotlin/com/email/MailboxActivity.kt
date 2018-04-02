package com.email

import android.view.Menu
import android.view.ViewGroup
import com.email.db.FeedLocalDB
import com.email.db.MailboxLocalDB
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.mailbox.*
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.feed.FeedModel
import com.email.scenes.mailbox.feed.ui.FeedView
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity() {

    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    private lateinit var notificationMenuClickListener: () -> Unit

    override fun initController(receivedModel: Any): SceneController {
        val db: MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        val model = receivedModel as MailboxSceneModel
        val activeAccount = ActiveAccount.loadFromStorage(this)
        val appDB = AppDatabase.getAppDatabase(this)
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB))
        val mailboxDataSource = MailboxDataSource(
                signalClient = signalClient,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount!!,
                mailboxLocalDB = db)

        mailboxDataSource.seed()
        val rootView = findViewById<ViewGroup>(R.id.drawer_layout)
        val scene = MailboxScene.MailboxSceneView(
                mailboxView = rootView,
                hostActivity = this,
                threadList = VirtualEmailThreadList(model.threads)
        )

        val controller = MailboxSceneController(
                scene = scene,
                model = model,
                host = this,
                dataSource = mailboxDataSource,
                feedController = initFeedController(model.feedModel)
        )

        notificationMenuClickListener = controller.menuClickListener
        return controller
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.mailbox_bell_container)?.actionView?.setOnClickListener {
            notificationMenuClickListener()
        }
        return true
    }

    private fun initFeedController(feedModel: FeedModel): FeedController{
        val db : FeedLocalDB.Default = FeedLocalDB.Default(this.applicationContext)
        db.seed()
        val feedView = FeedView.Default(
                feedItemsList = VirtualList.Map(feedModel.feedItems, { t -> ActivityFeedItem(t)}),
                container = findViewById(R.id.nav_right_view)
        )
        return FeedController(feedModel, feedView, FeedDataSource(AsyncTaskWorkRunner(), db))
    }


    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }


    private class VirtualEmailThreadList(val threads: ArrayList<EmailThread>)
        : VirtualList<EmailThread> {
        override fun get(i: Int) = threads[i]

        override val size: Int
            get() = threads.size
    }

}
