package com.email.scenes.connection

import android.os.AsyncTask
import android.os.Handler
import com.email.IHostActivity
import com.email.scenes.SceneController
import com.email.scenes.signin.data.SignInDataSource

/**
 * Created by sebas on 3/1/18.
 */

class ConnectionSceneController(private val model: ConnectionSceneModel,
                                private val scene: ConnectionScene,
                                private val host : IHostActivity,
                                private val dataSource: SignInDataSource)
    : SceneController() {
    override val menuResourceId: Int? = null

    override fun onStart() {
        ConnectionUpdater().execute()
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    inner class ConnectionUpdater() : AsyncTask<String, Int, Unit>() {
        override fun doInBackground(vararg p0: String?): Unit {
            for (advance: Int in 1..100) {
                publishProgress(advance)
                Thread.sleep(100)
            }
            return
        }

        override fun onProgressUpdate(vararg values: Int?) {
        }

        override fun onPreExecute() {
            super.onPreExecute()
            this@ConnectionSceneController.scene.startLoadingAnimation()

            Handler().postDelayed({
                this@ConnectionSceneController.scene.startSucceedAnimation()
            }, 3000)
        }

    }
}
