package com.email.scenes.composer

import android.view.ViewGroup
import com.email.BaseActivity
import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerDataSource


class ComposerActivity : BaseActivity() {

    override val layoutId = R.layout.activity_composer
    override val toolbarId = R.id.toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as ComposerModel
        val view = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val scene = ComposerScene.Default(view)
        val dataSource = ComposerDataSource(AsyncTaskWorkRunner())

        return ComposerController(model = model, scene = scene, dataSource = dataSource, host = this)
    }
}
