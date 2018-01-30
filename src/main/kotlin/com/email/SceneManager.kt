package com.email

import android.view.ViewGroup
import com.email.DB.MailboxLocalDB
import com.email.androidui.SceneFactory
import com.email.scenes.SceneController
import com.email.scenes.SceneModel
import com.email.scenes.mailbox.MailboxSceneController
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.data.MailboxDataSource

/**
 * Created by sebas on 1/30/18.
 */

class SceneManager(val hostActivity: IHostActivity) {

    private val sceneFactory = SceneFactory.SceneInflater(hostActivity)
    private var activeSceneController: SceneController

    private val rootLayout : ViewGroup = (hostActivity.activity as MainActivity)
            .findViewById(R.id.scene_container) as ViewGroup

    init {
        val model = MailboxSceneModel()
        activeSceneController = getControllerForModel(model)
    }

    private fun getControllerForModel(model: SceneModel): SceneController {
        return when (model) {
            is MailboxSceneModel -> MailboxSceneController(
                    scene = sceneFactory.createMailboxScene(),
                    model = model, dataSource = MailboxDataSource(MailboxLocalDB.Default(hostActivity.activity.applicationContext)))
            else -> throw UnsupportedOperationException("Can't generate controller with ${model.javaClass}")
        }
    }

    fun onStart() {
        activeSceneController.onStart()
    }
}
