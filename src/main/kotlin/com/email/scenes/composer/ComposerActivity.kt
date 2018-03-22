package com.email.scenes.composer

import android.app.Activity
import android.view.ViewGroup
import com.email.BaseActivity
import com.email.IHostActivity
import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.AppDatabase
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.scenes.SceneController
import com.email.scenes.composer.data.ComposerDataSource
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext


class ComposerActivity : BaseActivity() {

    override val layoutId = R.layout.activity_composer
    override val toolbarId = R.id.toolbar

    override fun initController(receivedModel: Any): SceneController {
        val model = receivedModel as ComposerModel
        return Companion.initController(AppDatabase.getAppDatabase(this), this, this, model)
    }

    companion object {
        fun initController(appDB: AppDatabase, activity: Activity, hostActivity: IHostActivity,
                           model: ComposerModel): ComposerController {
            val view = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
            val scene = ComposerScene.Default(view)
            val activeAccount = ActiveAccount.loadFromStorage(activity)
            val dataSource = ComposerDataSource(
                    signalClient = SignalClient.Default(SignalStoreCriptext(appDB)),
                    activeAccount = activeAccount!!,
                    rawSessionDao = appDB.rawSessionDao(),
                    runner = AsyncTaskWorkRunner())

            return ComposerController(model = model, scene = scene, dataSource = dataSource,
                    host = hostActivity)
        }
    }
}
