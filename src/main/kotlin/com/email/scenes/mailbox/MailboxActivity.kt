package com.email.scenes.mailbox

import android.app.Activity
import android.view.Menu
import android.view.ViewGroup
import com.email.BaseActivity
import com.email.IHostActivity
import com.email.R
import com.email.db.MailboxLocalDB
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.DeliveryTypes
import com.email.db.models.ActiveAccount
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.EmailContactIds
import com.email.scenes.SceneController
import com.email.scenes.mailbox.feed.data.ActivityFeedItem
import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.scenes.mailbox.feed.FeedController
import com.email.scenes.mailbox.feed.FeedModel
import com.email.scenes.mailbox.feed.ui.FeedView
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.utils.virtuallist.VirtualList
import com.email.websocket.WebSocket
import java.util.*

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity() {

    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    private lateinit var notificationMenuClickListener: () -> Unit

    // Only use this during development
    private fun seedEmails(appDB: AppDatabase) {
        val contacts = listOf(Contact(1,"mayer@jigl.com", "Mayer Mizrachi"),
                Contact(2, "gabriel@jigl.com", "Gabriel Aumala"))
        appDB.mailboxDao().insertContacts(contacts)
        val dateMilis = System.currentTimeMillis()
        val emails = (1..50)
          .map {
              val email = Email(id = 0, key = it.toString(), threadid = "thread$it", unread = true,
                  secure = true, content = "this is message #$it", preview =  "message #$it",
                  subject = "message #$it", delivered = DeliveryTypes.DELIVERED,
                  date = Date(dateMilis + it), isTrash = false, isDraft = false)
              val senderId = 1L
              val toIds = listOf(2L)
              EmailContactIds(email = email, toIds = toIds, ccIds = emptyList(),
                      bccIds = emptyList(), senderId = senderId)
          }
        appDB.mailboxDao().insertNewReceivedEmails(emails)
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
                    feedItemsList = VirtualList.Map(feedModel.feedItems, { t -> ActivityFeedItem(t)}),
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
            val webSocketEvents = WebSocket.newInstance(
                    activeAccount = activeAccount!!,
                    context = activity).webSocketController

            val mailboxDataSource = MailboxDataSource(
                signalClient = signalClient,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                rawSessionDao = appDB.rawSessionDao(),
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
