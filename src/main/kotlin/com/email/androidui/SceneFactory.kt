package com.email.androidui

import android.view.View
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.LabelChooser.LabelChooserScene
import com.email.scenes.mailbox.MailboxScene
import com.email.scenes.mailbox.data.EmailThread

/**
 * Instantiates scenes, the view objects for each controller.
 * Created by gabriel on 7/17/17.
 */

interface SceneFactory {

    fun createMailboxScene(): MailboxScene
    fun createChooserDialogScene(): LabelChooserScene

    class SceneInflater(val hostActivity: IHostActivity,
                        val threadListHandler: MailboxActivity.ThreadListHandler,
                        val labelThreadListHandler: MailboxActivity.LabelThreadListHandler)
        : SceneFactory {
        override fun createChooserDialogScene(): LabelChooserScene {
            return LabelChooserScene.LabelChooserView(rootLayout, hostActivity, labelThreadListHandler)
        }

        override fun createMailboxScene(): MailboxScene {
            val view = View.inflate(act, R.layout.activity_mailbox, null)
            return MailboxScene.MailboxSceneView(rootLayout, view, hostActivity, threadListHandler)
        }

        private val act = hostActivity as MailboxActivity
        private val rootLayout : ViewGroup = act.findViewById<ViewGroup>(R.id.scene_container) as ViewGroup

    }

}