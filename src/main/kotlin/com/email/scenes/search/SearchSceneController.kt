package com.email.scenes.search.holders

import com.email.scenes.SceneController
import com.email.scenes.search.SearchScene
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.ui.SearchResultListController

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneController(private val scene: SearchScene,
                            private val model: SearchSceneModel, private val dataSource: SearchDataSource): SceneController(){

    private val searchListController = SearchResultListController(model.results)

    override val menuResourceId: Int?
        get() = null

    override fun onOptionsItemSelected(itemId: Int) {

    }

    override fun onStart() {
        scene.attachView()
        dataSource.seed()
        val results = dataSource.getHistorySearch()
        searchListController.setSearchList(results)
    }

    override fun onStop() {

    }

    override fun onBackPressed(): Boolean {
        return scene.onBackPressed()
    }

}