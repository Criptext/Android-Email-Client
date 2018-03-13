package com.email.scenes.emaildetail

import com.email.IHostActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.data.EmailDetailDataSource

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailSceneController(private val scene: EmailDetailScene,
                                 private val model: EmailDetailSceneModel,
                                 private val host: IHostActivity,
                                 private val dataSource: EmailDetailDataSource) : SceneController() {

    override fun onStart() {

    }

    override fun onStop() {
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onOptionsItemSelected(itemId: Int) {
    }

    override val menuResourceId: Int?
        get() = R.menu.mailbox_menu_multi_mode_read
}
