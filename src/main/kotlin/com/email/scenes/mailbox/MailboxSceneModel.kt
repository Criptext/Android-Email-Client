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
    var threads : ArrayList<EmailThread> = ArrayList()
    var isInMultiSelect = false
    val selectedThreads = SelectedThreads()

    val getThreadFromIndex = {
        i: Int ->
        threads[i]
    }
    val getEmailThreadsCount = {
        threads.size
    }
}
