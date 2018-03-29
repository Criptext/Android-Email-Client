package com.email.scenes.mailbox

import com.email.db.LabelTextTypes
import com.email.db.models.FullEmail
import com.email.scenes.SceneModel
import com.email.scenes.composer.ui.UIData
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.feed.FeedModel

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    var label: LabelTextTypes = LabelTextTypes.INBOX // default label
    val threads : ArrayList<EmailThread> = ArrayList()
    var isInMultiSelect = false
    val selectedThreads = SelectedThreads()
    val hasSelectedUnreadMessages: Boolean
        get() = selectedThreads.hasUnreadThreads
    val feedModel = FeedModel()
    val isInUnreadMode: Boolean
        get() = selectedThreads.isInUnreadMode
    var shouldShowPartialUpdateInUI = false
    var mailToSend: UIData? = null

    val offset = 10 // We load 10 emails in each scroll
    var oldestEmailThread: EmailThread? = null
        get() = if(threads.isEmpty()) null else threads.last()
    var reachEnd = false

}
