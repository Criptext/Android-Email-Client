package com.criptext.mail.scenes.settings.cloudbackup

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.ExternalActivityParams
import com.criptext.mail.R
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.mailbox.ui.GoogleSignInObserver
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupDataSource
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.websocket.WebSocketSingleton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.util.*


class CloudBackupActivity: BaseActivity(){

    override val layoutId = R.layout.activity_cloud_backup
    override val toolbarId = null

    private lateinit var googleSignInListener: GoogleSignInObserver

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as CloudBackupModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = CloudBackupScene.Default(view)
        val appDB = AppDatabase.getAppDatabase(this)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val storage = KeyValueStorage.SharedPrefs(this)

        val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
        val webSocketEvents = if(jwts.isNotEmpty())
            WebSocketSingleton.getInstance(jwts)
        else
            WebSocketSingleton.getInstance(activeAccount.jwt)

        val dataSource = CloudBackupDataSource(
                runner = AsyncTaskWorkRunner(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                db = appDB,
                storage = storage,
                filesDir = this.filesDir
        )
        val controller = CloudBackupController(
                model = model,
                scene = scene,
                websocketEvents = webSocketEvents,
                keyboardManager = KeyboardManager(this),
                storage = storage,
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this,
                dataSource = dataSource)
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
        }
    }

}