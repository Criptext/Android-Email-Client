package com.criptext.mail.scenes.import_mailbox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
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
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
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

class ImportMailboxActivity: BaseActivity(){

    override val layoutId = R.layout.activity_import_mailbox
    override val toolbarId = null

    private lateinit var googleSignInListener: GoogleSignInObserver

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val model = receivedModel as ImportMailboxModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = ImportMailboxScene.Default(view)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val appDB = AppDatabase.getAppDatabase(this)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount))
        val storage = KeyValueStorage.SharedPrefs(this)

        val jwts = storage.getString(KeyValueStorage.StringKey.JWTS, "")
        val webSocketEvents = if(jwts.isNotEmpty())
            WebSocketSingleton.getInstance(jwts)
        else
            WebSocketSingleton.getInstance(activeAccount.jwt)

        val generalDataSource = GeneralDataSource(
                signalClient = signalClient,
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = storage,
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir,
                cacheDir = this.cacheDir
        )
        val controller = ImportMailboxController(
                model = model,
                scene = scene,
                websocketEvents = webSocketEvents,
                generalDataSource = generalDataSource,
                storage = storage,
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                host = this)
        googleSignInListener = controller.googleSignInListener
        return controller
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
            ExternalActivityParams.WRITE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data != null && data.data != null) {
                            setActivityMessage(ActivityMessage.SaveFileToLocalStorage(data.data!!))
                        }
                    }
                }
            }
        }
    }

}