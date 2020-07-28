package com.criptext.mail.scenes.search

import android.os.Bundle
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SearchLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.search.data.SearchDataSource
import com.criptext.mail.utils.KeyboardManager
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchActivity : BaseActivity() {

    override val layoutId = R.layout.search_layout
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any, savedInstanceState: Bundle?): SceneController {
        val appDB = AppDatabase.getAppDatabase(this)
        val db : SearchLocalDB.Default = SearchLocalDB.Default(appDB, this.filesDir)
        val model = receivedModel as SearchSceneModel
        val scene = SearchScene.SearchSceneView(findViewById(R.id.rootView), KeyboardManager(this))
        val activeAccount = ActiveAccount.loadFromStorage(this)!!
        val storage = KeyValueStorage.SharedPrefs(this.applicationContext)
        return SearchSceneController(
                scene = scene,
                model = model,
                host = this,
                storage = storage,
                activeAccount = activeAccount,
                dataSource = SearchDataSource(db, activeAccount, AsyncTaskWorkRunner()),
                generalDataSource = GeneralDataSource(
                        storage = storage,
                        httpClient = HttpClient.Default(),
                        db = appDB,
                        activeAccount = activeAccount,
                        eventLocalDB = EventLocalDB(appDB, this.filesDir, this.cacheDir),
                        filesDir = this.filesDir,
                        runner = AsyncTaskWorkRunner(),
                        signalClient = null,
                        cacheDir = this.cacheDir
                ))
    }
}