package com.email

import android.support.design.widget.NavigationView
import android.view.ViewGroup
import com.email.DB.FeedLocalDB
import com.email.DB.MailboxLocalDB
import com.email.DB.models.FeedItem
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

class MailboxActivity : BaseActivity(), IHostActivity {
    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(): SceneController {
        val DB: MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        val model = MailboxSceneModel()
        val feedModel = FeedModel()
        val rootView = findViewById<ViewGroup>(R.id.drawer_layout)
        val scene = MailboxScene.MailboxSceneView(rootView, this,
                VirtualEmailThreadList(model.threads))
        val feedScene = DrawerFeedView(VirtualFeedList(feedModel.feedItems),
                findViewById<NavigationView>(R.id.nav_right_view))
        return MailboxSceneController(
                scene = scene,
                model = model,
                dataSource = MailboxDataSource(DB),
                feedController = initFeedController(feedModel),
                feedScene = feedScene)
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

    private class VirtualFeedList(val feedItems: ArrayList<FeedItem>)
        : VirtualList<ActivityFeedItem> {

        override fun get(i: Int): ActivityFeedItem {
            return ActivityFeedItem(feedItems[i])
        }

        override val size: Int
            get() = feedItems.size
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

}
