package com.email.scenes.mailbox

import android.app.Activity
import android.view.Menu
import android.view.ViewGroup
import com.email.BaseActivity
import com.email.IHostActivity
import com.email.R
import com.email.api.HttpClient
import com.email.api.models.EmailMetadata
import com.email.db.MailboxLocalDB
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.models.*
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailInsertionSetup
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.feed.FeedModel
import com.email.scenes.mailbox.feed.ui.FeedView
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.utils.virtuallist.VirtualList
import com.email.websocket.WebSocketSingleton

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity() {

    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    private lateinit var notificationMenuClickListener: () -> Unit

    // Only use this during development
    private fun seedEmails(appDB: AppDatabase) {
        val fromContact = Contact(1,"mayer@jigl.com", "Mayer Mizrachi")
        (1..50)
          .forEach {
              val seconds = if (it < 10) "0$it" else it.toString()
              val metadata = EmailMetadata(from = "Mayer Mizrachi <mayer@jigl.com>",
                      to = "gabriel@jigl.com",  cc = "", bcc = "", fromContact = fromContact,
                      messageId = "gabriel/1/$it", date = "2018-02-21 14:00:$seconds",
                      threadId = "thread#$it", fromRecipientId = "mayer", subject = "Test #$it")
              val decryptedBody = "Hello, this is message #$it"
              val labels = listOf(Label.defaultItems.inbox)
              appDB.emailInsertionDao().runTransaction(Runnable {
                  EmailInsertionSetup.exec(appDB.emailInsertionDao(), metadata, decryptedBody, labels)
              })
          }

    }

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as MailboxSceneModel
        val appDB = AppDatabase.getAppDatabase(this)

        // seedEmails(appDB)
        val controller =  initController(
                appDB = appDB,
                hostActivity = this,
                activity = this,
                model = model)
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

    companion object {
        private fun initFeedController(appDB: AppDatabase, activity: Activity,
                                       feedModel: FeedModel): FeedController {
            val feedView = FeedView.Default(
                    feedItemsList = VirtualList.Map(feedModel.feedItems,
                            { t: FeedItem -> ActivityFeedItem(t)}),
                    container = activity.findViewById(R.id.nav_right_view)
            )
            return FeedController(feedModel, feedView, FeedDataSource(AsyncTaskWorkRunner(),
                    appDB.feedDao()))
        }

        fun initController(appDB: AppDatabase,
                           activity: Activity,
                           hostActivity: IHostActivity,
                           model: MailboxSceneModel): MailboxSceneController {
            val db: MailboxLocalDB.Default = MailboxLocalDB.Default(appDB)
            val signalClient = SignalClient.Default(SignalStoreCriptext(appDB))
            val activeAccount = ActiveAccount.loadFromStorage(activity)
            val webSocketEvents = WebSocketSingleton.getInstance(
                    activeAccount = activeAccount!!,
                    context = activity)

            val mailboxDataSource = MailboxDataSource(
                signalClient = signalClient,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                rawSessionDao = appDB.rawSessionDao(),
                    httpClient = HttpClient.Default(),
                emailInsertionDao = appDB.emailInsertionDao(),
                mailboxLocalDB = db)

            val rootView = activity.findViewById<ViewGroup>(R.id.drawer_layout)

            val scene = MailboxScene.MailboxSceneView(
                        mailboxView = rootView,
                        hostActivity = hostActivity
                )

            return MailboxSceneController(
                    scene = scene,
                    model = model,
                    host = hostActivity,
                    dataSource = mailboxDataSource,
                    websocketEvents = webSocketEvents,
                    feedController = initFeedController(appDB, activity, model.feedModel)
            )
        }
    }



}
