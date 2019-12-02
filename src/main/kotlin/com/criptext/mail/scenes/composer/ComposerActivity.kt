package com.criptext.mail.scenes.composer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.ComposerLocalDB
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.composer.data.ComposerDataSource
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.PhotoUtil
import com.criptext.mail.utils.file.ActivityMessageUtils
import com.criptext.mail.utils.file.FileUtils
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import droidninja.filepicker.FilePickerConst


class ComposerActivity : BaseActivity() {

    override val layoutId = R.layout.activity_composer
    override val toolbarId = R.id.toolbar

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val httpClient = HttpClient.Default(Hosts.fileServiceUrl, HttpClient.AuthScheme.jwt, 14000L, 7000L)
        val model = receivedModel as ComposerModel
        model.firstTime = savedInstanceState == null
        val view = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val appDB = AppDatabase.getAppDatabase(this)
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val signalClient = SignalClient.Default(SignalStoreCriptext(appDB, activeAccount))
        val scene = ComposerScene.Default(view, KeyboardManager(this))
        val db = ComposerLocalDB(contactDao = appDB.contactDao(), emailDao = appDB.emailDao(),
                emailLabelDao = appDB.emailLabelDao(), emailContactDao = appDB.emailContactDao(),
                labelDao = appDB.labelDao(), accountDao = appDB.accountDao(),
                fileDao = appDB.fileDao(), fileKeyDao = appDB.fileKeyDao(), filesDir = this.filesDir)
        val remoteChangeDataSource = GeneralDataSource(
                signalClient = signalClient,
                eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                storage = KeyValueStorage.SharedPrefs(this),
                db = appDB,
                runner = AsyncTaskWorkRunner(),
                activeAccount = activeAccount,
                httpClient = HttpClient.Default(),
                filesDir = this.filesDir
        )
        val dataSource = ComposerDataSource(
                filesDir = this.filesDir,
                httpClient = httpClient,
                composerLocalDB = db,
                activeAccount = activeAccount,
                emailInsertionDao = appDB.emailInsertionDao(),
                storage = KeyValueStorage.SharedPrefs(this),
                runner = AsyncTaskWorkRunner())
        return ComposerController(
                storage = KeyValueStorage.SharedPrefs(this),
                model = model,
                scene = scene,
                activeAccount = activeAccount,
                generalDataSource = remoteChangeDataSource,
                dataSource = dataSource,
                host = this)
    }

    private fun setNewAttachmentsAsActivityMessage(data: Intent?, filePickerConst: String?) {
        when(filePickerConst){
            FilePickerConst.KEY_SELECTED_MEDIA,
            FilePickerConst.KEY_SELECTED_DOCS -> {
                if(data != null) {
                    setActivityMessage(ActivityMessageUtils.getAddAttachmentsActivityMessage(data, contentResolver, this, false))
                }
            }
            PhotoUtil.KEY_PHOTO_TAKEN -> {
                val photo= photoUtil.getPhotoFileFromIntent()
                if(photo != null && photo.length() != 0L)
                    setActivityMessage(ActivityMessage.AddAttachments(listOf(Pair(photo.absolutePath, photo.length())), false))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data != null) {
            when (requestCode){
                FilePickerConst.REQUEST_CODE_DOC -> setNewAttachmentsAsActivityMessage(data, FilePickerConst.KEY_SELECTED_DOCS)

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
