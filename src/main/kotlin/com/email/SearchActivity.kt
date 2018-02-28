package com.email

import com.email.db.SearchLocalDB
import com.email.scenes.SceneController
import com.email.scenes.search.SearchScene
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.holders.SearchSceneController
import com.email.utils.VirtualList

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchActivity : BaseActivity() {

    override val layoutId = R.layout.search_layout
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(receivedModel: Any): SceneController {
        val DB : SearchLocalDB.Default = SearchLocalDB.Default(this.applicationContext)
        val model = receivedModel as SearchSceneModel
        val scene = SearchScene.SearchSceneView(this, VirtualSearchList(model.results))
        return SearchSceneController(
                scene,
                model,
                SearchDataSource(DB))
    }

    private class VirtualSearchList(val results: ArrayList<SearchResult>)
        : VirtualList<SearchResult> {
        override fun get(i: Int) = results[i]

        override val size: Int
            get() = results.size
    }
}