package com.email.scenes.composer

import android.content.Intent
import android.view.ViewGroup
import com.email.BaseActivity
import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.ComposerLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.ActivityMessage
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerDataSource
import droidninja.filepicker.FilePickerConst

class ComposerActivity : BaseActivity() {

    override val layoutId = R.layout.activity_composer
    override val toolbarId = R.id.toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as ComposerModel
        val view = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val appDB = AppDatabase.getAppDatabase(this)
        val scene = ComposerScene.Default(view)
        val db = ComposerLocalDB(appDB.contactDao(), appDB.emailDao(), appDB.labelDao(),
                appDB.emailLabelDao(), appDB.emailContactDao(), appDB.accountDao())
        val dataSource = ComposerDataSource(
                composerLocalDB = db,
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                emailInsertionDao = appDB.emailInsertionDao(),
                runner = AsyncTaskWorkRunner())
        return ComposerController(
                model = model,
                scene = scene,
                dataSource = dataSource,
                host = this)
    }

    private fun setNewAttachmentsAsActivityMessage(data: Intent) {
        val selectedAttachments = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS)
        setActivityMessage(ActivityMessage.AddAttachments(selectedAttachments))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FilePickerConst.REQUEST_CODE_DOC && data != null) {
            setNewAttachmentsAsActivityMessage(data)
        }
    }
}
