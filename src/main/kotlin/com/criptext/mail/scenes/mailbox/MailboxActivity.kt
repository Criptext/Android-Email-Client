package com.criptext.mail.scenes.mailbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.*
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.MailboxDataSource
import com.criptext.mail.scenes.mailbox.feed.FeedController
import com.criptext.mail.scenes.mailbox.feed.FeedModel
import com.criptext.mail.scenes.mailbox.feed.data.FeedDataSource
import com.criptext.mail.scenes.mailbox.feed.ui.FeedScene
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.file.ActivityMessageUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketSingleton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import droidninja.filepicker.FilePickerConst
import java.util.*


/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity() {

    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    private lateinit var notificationMenuClickListener: () -> Unit
    private lateinit var googleSignInListener: GoogleSignInObserver

    // Only use this during development
    private fun seedEmails(appDB: AppDatabase) {
        val fromContact = Contact(1,"mayer@jigl.com", "Mayer Mizrachi", true, 0, 0)
        (1..50)
          .forEach {
              val seconds = if (it < 10) "0$it" else it.toString()
              val metadata = EmailMetadata.DBColumns(to = listOf("gabriel@jigl.com"),  cc = emptyList(), bcc = emptyList(),
                      fromContact = fromContact, messageId = "gabriel/1/$it",
                      date = "2018-02-21 14:00:$seconds",unsentDate = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                      subject = "Test #$it", unread = true, metadataKey = 1 + 100,
                      status = DeliveryTypes.NONE, secure = true, trashDate = "2018-02-21 14:00:$seconds",
                      replyTo = null, boundary = null)
              val decryptedBody = "Hello, this is message #$it"
              val labels = listOf(Label.defaultItems.inbox)
              appDB.emailInsertionDao().runTransaction {
                  EmailInsertionSetup.exec(appDB.emailInsertionDao(), metadata, decryptedBody,
                          labels, emptyList(), null, 1)
              }
          }

    }

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
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
        googleSignInListener = controller.googleSignInListener
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
                    appDB.feedDao(), appDB.emailDao(), appDB.contactDao(), appDB.fileDao(), account))
        }

        fun initController(appDB: AppDatabase,
                           activity: Activity,
                           hostActivity: IHostActivity,
                           model: MailboxSceneModel,
                           storage: KeyValueStorage): MailboxSceneController {

            val db: MailboxLocalDB.Default = MailboxLocalDB.Default(appDB, activity.filesDir)
            val activeAccount = ActiveAccount.loadFromStorage(activity)!!
            val signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount))

            val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
            val webSocketEvents = if(jwts.isNotEmpty())
                WebSocketSingleton.getInstance(jwts)
            else
                WebSocketSingleton.getInstance(activeAccount.jwt)

            val mailboxDataSource = MailboxDataSource(
                filesDir = activity.filesDir,
                signalClient = signalClient,
                storage = storage,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                pendingDao = appDB.pendingEventDao(),
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
                eventLocalDB = EventLocalDB(appDB, activity.filesDir, activity.cacheDir),
                db = appDB)

            val rootView = activity.findViewById<ViewGroup>(R.id.drawer_layout)

            val scene = MailboxScene.MailboxSceneView(
                        mailboxView = rootView,
                        hostActivity = hostActivity
                )
            val remoteChangeDataSource = GeneralDataSource(
                    signalClient = signalClient,
                    eventLocalDB = EventLocalDB(appDB, activity.filesDir, activity.cacheDir),
                    storage = storage,
                    db = appDB,
                    runner = AsyncTaskWorkRunner(),
                    activeAccount = activeAccount,
                    httpClient = HttpClient.Default(),
                    filesDir = activity.filesDir
            )

            return MailboxSceneController(
                    scene = scene,
                    model = model,
                    host = hostActivity,
                    storage = storage,
                    activeAccount = activeAccount,
                    generalDataSource = remoteChangeDataSource,
                    dataSource = mailboxDataSource,
                    websocketEvents = webSocketEvents,
                    feedController = initFeedController(appDB, activity, db,
                            model.feedModel, hostActivity, activeAccount)
            )
        }
    }

    private fun setNewAttachmentsAsActivityMessage(data: Intent?, filePickerConst: String?) {
        when(filePickerConst){
            FilePickerConst.KEY_SELECTED_DOCS -> {
                if(data != null) {
                    setActivityMessage(ActivityMessageUtils.getAddAttachmentsActivityMessage(data, contentResolver, this, false))
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalActivityParams.REQUEST_CODE_SIGN_IN -> {
                when(resultCode){
                    Activity.RESULT_OK -> {
                        if(data != null){
                            GoogleSignIn.getSignedInAccountFromIntent(data)
                                    .addOnSuccessListener { googleAccount ->

                                        val credential = GoogleAccountCredential.usingOAuth2(
                                                this, Collections.singleton(DriveScopes.DRIVE_FILE))
                                        credential.selectedAccount = googleAccount.account
                                        val googleDriveService = Drive.Builder(
                                                NetHttpTransport(),
                                                GsonFactory(),
                                                credential)
                                                .setApplicationName("Criptext Secure Email")
                                                .build()

                                        googleSignInListener.signInSuccess(googleDriveService)
                                    }
                                    .addOnFailureListener { googleSignInListener.signInFailed() }
                        }
                    }
                }

            }
            FilePickerConst.REQUEST_CODE_DOC -> {
                setNewAttachmentsAsActivityMessage(data, FilePickerConst.KEY_SELECTED_DOCS)
            }
        }
    }



}
