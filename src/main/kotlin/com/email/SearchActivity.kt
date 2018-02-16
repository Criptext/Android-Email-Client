package com.email

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.email.DB.SearchLocalDB
import com.email.scenes.SceneController
import com.email.scenes.search.SearchScene
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.holders.SearchSceneController
import com.email.utils.VirtualList
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchActivity : BaseActivity() {

    override val layoutId = R.layout.search_layout
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(): SceneController {
        val DB : SearchLocalDB.Default = SearchLocalDB.Default(this.applicationContext)
        val model = SearchSceneModel()
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