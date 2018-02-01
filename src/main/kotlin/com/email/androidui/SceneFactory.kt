package com.email.androidui

import android.view.View
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.mailbox.MailboxScene
import com.email.scenes.mailbox.data.EmailThread

/**
 * Instantiates scenes, the view objects for each controller.
 * Created by gabriel on 7/17/17.
 */

interface SceneFactory {

    fun createMailboxScene(): MailboxScene

    class SceneInflater(val hostActivity: IHostActivity,
                        val getThreadFromIndex: (i: Int) -> EmailThread,
                        val getEmailThreadsCount: () -> Int)
        : SceneFactory {
        override fun createMailboxScene(): MailboxScene {
            val view = View.inflate(act, R.layout.activity_mailbox, null)
            return MailboxScene.MailboxSceneView(rootLayout, view, hostActivity, getThreadFromIndex, getEmailThreadsCount)
        }

        private val act = hostActivity as MailboxActivity
        private val rootLayout : ViewGroup = act.findViewById<ViewGroup>(R.id.scene_container) as ViewGroup

    }

}