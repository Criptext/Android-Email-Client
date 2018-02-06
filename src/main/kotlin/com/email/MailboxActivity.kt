package com.email

import android.view.ViewGroup
import com.email.DB.MailboxLocalDB
import com.email.scenes.SceneController
import com.email.scenes.mailbox.*
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.MailboxDataSource
import com.email.utils.VirtualList

/**
 * Created by sebas on 1/30/18.
 */

class MailboxActivity : BaseActivity(), IHostActivity {
    override val layoutId = R.layout.activity_mailbox
    override val toolbarId = R.id.mailbox_toolbar

    override fun initController(): SceneController {
        val DB: MailboxLocalDB.Default = MailboxLocalDB.Default(this.applicationContext)
        val model = MailboxSceneModel()
        val rootView = findViewById<ViewGroup>(R.id.drawer_layout)
        val scene = MailboxScene.MailboxSceneView(rootView, this,
                VirtualEmailThreadList(model.threads))
        return MailboxSceneController(
                scene = scene,
                model = model,
                dataSource = MailboxDataSource(DB))
    }

    private class VirtualEmailThreadList(val threads: ArrayList<EmailThread>)
        : VirtualList<EmailThread> {
        override fun get(i: Int) = threads[i]

        override val size: Int
            get() = threads.size
    }

    override fun refreshToolbarItems() {
        this.invalidateOptionsMenu()
    }

}
