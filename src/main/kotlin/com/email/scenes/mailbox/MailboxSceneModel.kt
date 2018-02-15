package com.email.scenes.mailbox

import com.email.scenes.SceneModel
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 1/25/18.
 */

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    val threads : ArrayList<EmailThread> = ArrayList()
    var isInMultiSelect = false
    val selectedThreads = SelectedThreads()
    val hasSelectedUnreadMessages: Boolean
        get() = selectedThreads.hasUnreadThreads
    val isInUnreadMode: Boolean
        get() = selectedThreads.isInUnreadMode
}
