package com.email.androidui

import android.view.View
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.mailbox.MailboxScene

/**
 * Instantiates scenes, the view objects for each controller.
 * Created by gabriel on 7/17/17.
 */

interface SceneFactory {

    fun createMailboxScene(): MailboxScene

    class SceneInflater(val hostActivity: IHostActivity,
                        val threadListHandler: MailboxActivity.ThreadListHandler)
        : SceneFactory {

        override fun createMailboxScene(): MailboxScene {
            val view = View.inflate(act, R.layout.activity_mailbox, null)
            return MailboxScene.MailboxSceneView(rootLayout, view, hostActivity, threadListHandler)
        }

        private val act = hostActivity as MailboxActivity
        private val rootLayout : ViewGroup = act.findViewById(R.id.scene_container) as ViewGroup

    }

}