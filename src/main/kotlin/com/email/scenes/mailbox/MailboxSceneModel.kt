package com.email.scenes.mailbox

import com.email.db.LabelTextTypes
import com.email.scenes.SceneModel
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.feed.FeedModel

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    var label: LabelTextTypes = LabelTextTypes.INBOX // default
    val threads : ArrayList<EmailThread> = ArrayList()
    var isInMultiSelect = false
    val selectedThreads = SelectedThreads()
    val hasSelectedUnreadMessages: Boolean
        get() = selectedThreads.hasUnreadThreads
    val feedModel = FeedModel()
    val isInUnreadMode: Boolean
        get() = selectedThreads.isInUnreadMode
    var shouldShowPartialUpdateInUI = false
}
