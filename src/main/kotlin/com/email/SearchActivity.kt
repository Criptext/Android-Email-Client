package com.email

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.email.DB.SearchLocalDB
import com.email.scenes.search.SearchScene
import com.email.scenes.search.SearchSceneModel
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.holders.SearchSceneController
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchActivity : AppCompatActivity(), IHostActivity {

    private lateinit var searchSceneController: SearchSceneController
    private var searchSceneModel : SearchSceneModel = SearchSceneModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_layout)

        initController()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun initController() {
        val DB : SearchLocalDB.Default = SearchLocalDB.Default(this.applicationContext)
        searchSceneController = SearchSceneController(
                SearchScene.SearchSceneView(this),
                searchSceneModel,
                SearchDataSource(DB))
    }

    override fun onStart() {
        super.onStart()
        searchSceneController.onStart()
    }

    override fun onBackPressed() {
        searchSceneController.onBackPressed(this)
    }
}