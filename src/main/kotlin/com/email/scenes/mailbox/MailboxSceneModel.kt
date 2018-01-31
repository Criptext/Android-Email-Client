package com.email.scenes.mailbox

import com.email.scenes.SceneModel
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/25/18.
 */

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    internal var threads : ArrayList<EmailThread> = ArrayList()
    internal var isInMultiSelect = false
    internal val selectedThreads = SelectedThreads()
}
