package com.email.scenes.emaildetail

import com.email.IHostActivity
import com.email.R
import com.email.scenes.SceneController
import com.email.scenes.emaildetail.data.EmailDetailDataSource

/**
 * Created by sebas on 3/12/18.
 */

class EmailDetailController(private val scene: EmailDetailScene,
                             private val model: EmailDetailSceneModel,
                             private val host: IHostActivity,
                             private val dataSource: EmailDetailDataSource) : SceneController() {

    override fun onStart() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBackPressed(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onOptionsItemSelected(itemId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val menuResourceId: Int?
        get() = R.menu.mailbox_menu_normal_mode
}
