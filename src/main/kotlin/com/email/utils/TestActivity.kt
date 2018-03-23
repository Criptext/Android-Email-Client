package com.email.utils

import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.email.IHostActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.params.SceneParams

/**
 * Created by gabriel on 3/21/18.
 */

class TestActivity: AppCompatActivity(), IHostActivity {

    override fun showDialog(message: UIMessage) {
    }

    override fun dismissDialog() {
    }

    lateinit var controller: SceneController

    override fun refreshToolbarItems() {
        invalidateOptionsMenu()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val activeSceneMenu = controller.menuResourceId
        if(activeSceneMenu != null) menuInflater.inflate(activeSceneMenu, menu)
        return true
    }

    override fun goToScene(params: SceneParams) {
    }

    override fun finishScene() {
        finish()
    }

    override fun getLocalizedString(message: UIMessage): String {
        return this.getLocalizedString(message)
    }

    fun setLayoutOnUiThread(id: Int) {
        runOnUiThread { setContentView(id) }
    }

    fun startOnUiThread() {
        runOnUiThread { controller.onStart() }
    }
}