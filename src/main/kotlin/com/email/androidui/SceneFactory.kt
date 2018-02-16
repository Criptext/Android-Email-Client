package com.email.androidui

import android.view.View
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.mailbox.MailboxScene
import com.email.scenes.search.SearchScene

/**
 * Instantiates scenes, the view objects for each controller.
 * Created by gabriel on 7/17/17.
 */

interface SceneFactory {

    fun createMailboxScene(): MailboxScene
}