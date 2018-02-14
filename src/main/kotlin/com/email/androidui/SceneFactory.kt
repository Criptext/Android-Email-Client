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
}