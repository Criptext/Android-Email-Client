package com.email.scenes.search.holders

import android.app.Activity
import android.os.Bundle
import com.email.scenes.SceneController
import com.email.scenes.search.SearchScene
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.search.data.SearchDataSource

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneController(private val scene: SearchScene,
                            private val model: SearchSceneModel, private val dataSource: SearchDataSource): SceneController(){

    override fun onStart() {
        scene.attachView()
        dataSource.seed()
        model.results = dataSource.getHistorySearch()
        scene.setSearchResult(dataSource.getHistorySearch())
    }

    override fun onStop() {

    }

    override fun onBackPressed(activity: Activity) {
        scene.onBackPressed(activity)
    }

}