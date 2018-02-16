package com.email

import android.view.ViewGroup
import com.email.DB.FeedLocalDB
import com.email.DB.MailboxLocalDB
import com.email.scenes.SceneController
import com.email.scenes.mailbox.*
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.feed.FeedModel
import com.email.scenes.mailbox.ui.DrawerFeedView
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity() {
    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val DB: MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        val model = receivedModel as MailboxSceneModel
        val feedModel = model.feedModel

        val rootView = findViewById<ViewGroup>(R.id.drawer_layout)
        val scene = MailboxScene.MailboxSceneView(
                mailboxView = rootView,
                hostActivity = this,
                threadList = VirtualEmailThreadList(model.threads)
        )
        val feedScene = DrawerFeedView(
                feedItemsList = VirtualList.Map(feedModel.feedItems, { t -> ActivityFeedItem(t)}),
                navigationView = findViewById(R.id.nav_right_view)
        )

        return MailboxSceneController(
                scene = scene,
                model = model,
                host = this,
                dataSource = MailboxDataSource(DB),
                feedController = initFeedController(feedModel),
                feedScene = feedScene
        )
    }

    private fun initFeedController(feedModel: FeedModel): FeedController{

        val DBF : FeedLocalDB.Default = FeedLocalDB.Default(this.applicationContext)
        return FeedController(feedModel, FeedDataSource(DBF))
    }

    private class VirtualEmailThreadList(val threads: ArrayList<EmailThread>)
        : VirtualList<EmailThread> {
        override fun get(i: Int) = threads[i]

        override val size: Int
            get() = threads.size
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

}
