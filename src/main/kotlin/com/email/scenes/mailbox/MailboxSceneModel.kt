package com.email.scenes.mailbox

import com.email.db.MailFolders
import com.email.scenes.SceneModel
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.data.LoadingType
import com.email.scenes.mailbox.feed.FeedModel

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    var loadingType = LoadingType.FULL
    var label: MailFolders = MailFolders.INBOX // default label
    val threads : ArrayList<EmailThread> = ArrayList()
    val selectedThreads = SelectedThreads()
    val hasSelectedUnreadMessages: Boolean
        get() = selectedThreads.hasUnreadThreads
    val feedModel = FeedModel()
    val isInUnreadMode: Boolean
        get() = selectedThreads.isInUnreadMode
    val offset = 20 // We load 20 emails in each scroll
    var oldestEmailThread: EmailThread? = null
        get() = if(threads.isEmpty()) null else threads.last()
    var isInMultiSelect = false
    var hasReachedEnd = true
    var mailToSend: ComposerInputData? = null
}
