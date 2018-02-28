package com.email.scenes.keygeneration

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
    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

}