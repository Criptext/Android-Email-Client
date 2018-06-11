package com.email.scenes.search

import com.email.BaseActivity
import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.db.SearchLocalDB
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.search.data.SearchDataSource
import com.email.utils.KeyboardManager

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchActivity : BaseActivity() {

    override val layoutId = R.layout.search_layout
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val appDB = AppDatabase.getAppDatabase(this)
        val db : SearchLocalDB.Default = SearchLocalDB.Default(appDB)
        val model = receivedModel as SearchSceneModel
        val scene = SearchScene.SearchSceneView(findViewById(R.id.rootView), KeyboardManager(this))
        return SearchSceneController(
                scene = scene,
                model = model,
                host = this,
                storage = KeyValueStorage.SharedPrefs(this.applicationContext),
                activeAccount = ActiveAccount.loadFromStorage(this)!!,
                dataSource = SearchDataSource(db, AsyncTaskWorkRunner()))
    }
}