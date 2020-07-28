package com.criptext.mail.scenes.settings.profile

import android.content.Intent
import android.os.Bundle
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
import com.criptext.mail.scenes.settings.profile.data.ProfileDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PhotoUtil
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.websocket.WebSocketSingleton
import droidninja.filepicker.FilePickerConst
import okhttp3.internal.Util
import java.io.File


class ProfileActivity: BaseActivity(){

    override val layoutId = R.layout.activity_profile
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val model = receivedModel as ProfileModel
        val view = findViewById<ViewGroup>(R.id.main_content)
        val scene = ProfileScene.Default(view)
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
                activeAccount = activeAccount,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir,
                cacheDir = this.cacheDir
        )
        val dataSource = ProfileDataSource(
                cacheDir = cacheDir,
                storage = storage,
                accountDao = appDB.accountDao(),
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                httpClient = HttpClient.Default()
        )
        return ProfileController(
                activeAccount = activeAccount,
                model = model,
                scene = scene,
                storage = storage,
                websocketEvents = webSocketEvents,
                generalDataSource = generalDataSource,
                keyboardManager = KeyboardManager(this),
                dataSource = dataSource,
                host = this)
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
        if(data != null) {
            when (requestCode){
                FilePickerConst.REQUEST_CODE_PHOTO -> setNewAttachmentsAsActivityMessage(data, FilePickerConst.KEY_SELECTED_MEDIA)
            }
        }else{
            if (requestCode == PhotoUtil.REQUEST_CODE_CAMERA){
                setNewAttachmentsAsActivityMessage(null, PhotoUtil.KEY_PHOTO_TAKEN)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        controller.requestPermissionResult(requestCode, permissions, grantResults)
    }
}