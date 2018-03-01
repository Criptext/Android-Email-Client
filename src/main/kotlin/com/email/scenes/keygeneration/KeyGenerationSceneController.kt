package com.email.scenes.keygeneration

import android.os.AsyncTask
import com.email.IHostActivity
import com.email.scenes.SceneController
import com.email.scenes.keygeneration.data.KeyGenerationSceneModel
import com.email.scenes.signin.SignUpDataSource

/**
 * Created by sebas on 2/28/18.
 */

class KeyGenerationSceneController(private val model: KeyGenerationSceneModel,
                                   private val scene: KeyGenerationScene,
                                   private val host : IHostActivity,
                                   private val dataSource: SignUpDataSource)
    : SceneController() {
    override val menuResourceId: Int? = null

    override fun onStart() {
        KeyGenerationUpdater().execute()
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    inner class KeyGenerationUpdater() : AsyncTask<String, Int , Unit>() {
        override fun doInBackground(vararg p0: String?): Unit {
            for (advance: Int in 1..100) {
                publishProgress(advance)
                Thread.sleep(100)
            }
            return
        }

        override fun onProgressUpdate(vararg values: Int?) {
            this@KeyGenerationSceneController.scene.updateProgress(
                    progress=values[0]!!)

        }

        override fun onPreExecute() {
            super.onPreExecute()
            this@KeyGenerationSceneController.scene.updateProgress(progress = 0)
        }

    }
}