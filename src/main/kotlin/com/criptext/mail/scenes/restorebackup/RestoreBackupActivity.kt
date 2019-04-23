package com.criptext.mail.scenes.restorebackup

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.restorebackup.data.RestoreBackupDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketSingleton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*

class RestoreBackupActivity: BaseActivity(){

    override val layoutId = R.layout.activity_restore_backup
    override val toolbarId = null

    private lateinit var googleSignInListener: GoogleSignInObserver

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as RestoreBackupModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = RestoreBackupScene.Default(view)
        val appDB = AppDatabase.getAppDatabase(this)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount))
        val webSocketEvents = WebSocketSingleton.getInstance(
                activeAccount = activeAccount)

        val dataSource = RestoreBackupDataSource(
                storage = KeyValueStorage.SharedPrefs(this),
                runner = AsyncTaskWorkRunner(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                db = appDB,
                filesDir = this.filesDir
        )
        val controller = RestoreBackupController(
                model = model,
                scene = scene,
                websocketEvents = webSocketEvents,
                dataSource = dataSource,
                keyboardManager = KeyboardManager(this),
                storage = KeyValueStorage.SharedPrefs(this),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
        googleSignInListener = controller.googleSignInListener
        return controller
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
                                                AndroidHttp.newCompatibleTransport(),
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
        }
    }

}