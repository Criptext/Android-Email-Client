package com.criptext.mail.scenes.mailbox

import android.app.Activity
import android.view.Menu
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.*
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.feed.data.FeedDataSource
import com.criptext.mail.scenes.mailbox.data.MailboxDataSource
import com.criptext.mail.scenes.mailbox.feed.FeedController
import com.criptext.mail.scenes.mailbox.feed.FeedModel
import com.criptext.mail.scenes.mailbox.feed.ui.FeedScene
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.websocket.WebSocketSingleton
import android.content.Intent
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource


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
              val metadata = EmailMetadata.DBColumns(to = listOf("gabriel@jigl.com"),  cc = emptyList(), bcc = emptyList(),
                      fromContact = fromContact, messageId = "gabriel/1/$it",
                      date = "2018-02-21 14:00:$seconds",unsentDate = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                      subject = "Test #$it", unread = true, metadataKey = 1 + 100,
                      status = DeliveryTypes.NONE, secure = true, trashDate = "2018-02-21 14:00:$seconds")
              val decryptedBody = "Hello, this is message #$it"
              val labels = listOf(Label.defaultItems.inbox)
              appDB.emailInsertionDao().runTransaction({
                  EmailInsertionSetup.exec(appDB.emailInsertionDao(), metadata, decryptedBody,
                          labels, emptyList(), null)
              })
          }

    }

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as MailboxSceneModel
        val appDB = AppDatabase.getAppDatabase(this)
        val storage = KeyValueStorage.SharedPrefs(this)

        // seedEmails(appDB)
        val controller =  initController(
                appDB = appDB,
                hostActivity = this,
                activity = this,
                model = model,
                storage = storage
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {

        private fun initFeedController(appDB: AppDatabase, activity: Activity, mailboxLocalDB: MailboxLocalDB,
                                       feedModel: FeedModel, hostActivity: IHostActivity,
                                       account: ActiveAccount): FeedController {

            val feedView = FeedScene.Default(activity.findViewById(R.id.nav_right_view))
            return FeedController(
                    model = feedModel,
                    scene = feedView,
                    host = hostActivity,
                    activeAccount = account,
                    storage = KeyValueStorage.SharedPrefs(activity),
                    feedDataSource = FeedDataSource(AsyncTaskWorkRunner(), mailboxLocalDB,
                    appDB.feedDao(), appDB.emailDao(), appDB.contactDao(), appDB.fileDao()))
        }

        fun initController(appDB: AppDatabase,
                           activity: Activity,
                           hostActivity: IHostActivity,
                           model: MailboxSceneModel,
                           storage: KeyValueStorage): MailboxSceneController {

            val db: MailboxLocalDB.Default = MailboxLocalDB.Default(appDB)
            val signalClient = SignalClient.Default(SignalStoreCriptext(appDB))
            val activeAccount = ActiveAccount.loadFromStorage(activity)
            val webSocketEvents = WebSocketSingleton.getInstance(
                    activeAccount = activeAccount!!,
                    context = activity)

            val mailboxDataSource = MailboxDataSource(
                signalClient = signalClient,
                storage = storage,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                emailDao = appDB.emailDao(),
                accountDao = appDB.accountDao(),
                feedItemDao = appDB.feedDao(),
                contactDao = appDB.contactDao(),
                rawSessionDao = appDB.rawSessionDao(),
                rawIdentityKeyDao = appDB.rawIdentityKeyDao(),
                httpClient = HttpClient.Default(),
                emailInsertionDao = appDB.emailInsertionDao(),
                mailboxLocalDB = db,
                fileDao = appDB.fileDao(),
                fileKeyDao = appDB.fileKeyDao(),
                labelDao = appDB.labelDao(),
                emailLabelDao = appDB.emailLabelDao(),
                emailContactJoinDao = appDB.emailContactDao(),
                eventLocalDB = EventLocalDB(appDB))

            val rootView = activity.findViewById<ViewGroup>(R.id.drawer_layout)

            val scene = MailboxScene.MailboxSceneView(
                        mailboxView = rootView,
                        hostActivity = hostActivity
                )
            val remoteChangeDataSource = GeneralDataSource(
                    storage = storage,
                    db = appDB,
                    runner = AsyncTaskWorkRunner(),
                    activeAccount = activeAccount,
                    httpClient = HttpClient.Default()
            )

            return MailboxSceneController(
                    scene = scene,
                    model = model,
                    host = hostActivity,
                    activeAccount = activeAccount,
                    generalDataSource = remoteChangeDataSource,
                    dataSource = mailboxDataSource,
                    websocketEvents = webSocketEvents,
                    feedController = initFeedController(appDB, activity, db,
                            model.feedModel, hostActivity, activeAccount)
            )
        }
    }



}
