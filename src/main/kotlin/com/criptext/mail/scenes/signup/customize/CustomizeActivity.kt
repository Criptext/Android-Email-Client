package com.criptext.mail.scenes.signup.customize

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.criptext.mail.utils.PhotoUtil
import com.criptext.mail.utils.file.FileUtils
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

class CustomizeActivity: BaseActivity() {

    private lateinit var googleSignInListener: GoogleSignInObserver

    override val layoutId: Int = R.layout.activity_customize

    override val toolbarId: Int?
        get() = null

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val appDB = AppDatabase.getAppDatabase(this.applicationContext)
        val customizeSceneView = CustomizeScene.CustomizeSceneView(findViewById(R.id.customize_layout_container))
        val customizeSceneModel = receivedModel as CustomizeSceneModel
        val keyValueStorage = KeyValueStorage.SharedPrefs(this)

        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount))

        val jwts = keyValueStorage.getString(KeyValueStorage.StringKey.JWTS, "")
        val webSocketEvents = if(jwts.isNotEmpty())
            WebSocketSingleton.getInstance(jwts)
        else
            WebSocketSingleton.getInstance(activeAccount.jwt)

        val generalDataSource = GeneralDataSource(
                signalClient = signalClient,
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = keyValueStorage,
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir,
                cacheDir = this.cacheDir
        )

        val controller =  CustomizeSceneController(
                model = customizeSceneModel,
                keyboardManager = KeyboardManager(this),
                scene = customizeSceneView,
                host = this,
                generalDataSource = generalDataSource,
                storage = keyValueStorage,
                activeAccount = activeAccount,
                websocketEvents = webSocketEvents)
        googleSignInListener = controller.googleSignInListener
        return controller
    }

    private fun setNewAttachmentsAsActivityMessage(data: Intent?, filePickerConst: String?) {
        when(filePickerConst){
            FilePickerConst.KEY_SELECTED_MEDIA -> {
                if(data != null) {
                    val clipData = data.clipData
                    if(clipData == null) {
                        data.data?.also { uri ->
                            val attachment = FileUtils.getPathAndSizeFromUri(uri, contentResolver, this, data)
                            if (attachment != null)
                                setActivityMessage(ActivityMessage.ProfilePictureFile(attachment))
                        }
                    }
                }
            }
            PhotoUtil.KEY_PHOTO_TAKEN -> {
                val photo= photoUtil.getPhotoFileFromIntent()
                if(photo != null && photo.length() != 0L)
                    setActivityMessage(ActivityMessage.ProfilePictureFile(Pair(photo.absolutePath, photo.length())))
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
            FilePickerConst.REQUEST_CODE_PHOTO -> setNewAttachmentsAsActivityMessage(data, FilePickerConst.KEY_SELECTED_MEDIA)
            PhotoUtil.REQUEST_CODE_CAMERA -> {
                setNewAttachmentsAsActivityMessage(null, PhotoUtil.KEY_PHOTO_TAKEN)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller.requestPermissionResult(requestCode, permissions, grantResults)
    }
}