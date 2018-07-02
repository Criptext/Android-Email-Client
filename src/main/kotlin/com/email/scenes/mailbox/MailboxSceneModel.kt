package com.email.scenes.mailbox

import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.scenes.SceneModel
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.mailbox.feed.FeedModel

/**
 * Created by sebas on 1/25/18.
 */

class MailboxSceneModel : SceneModel {
    var selectedLabel: Label = Label.defaultItems.inbox // default label
    val threads : ArrayList<EmailPreview> = ArrayList()
    val selectedThreads = SelectedThreads()
    val hasSelectedUnreadMessages: Boolean
        get() = selectedThreads.hasUnreadThreads
    val feedModel = FeedModel()
    val isInUnreadMode: Boolean
        get() = selectedThreads.isInUnreadMode
    var isInMultiSelect = false
    var hasReachedEnd = true
    var lastSync = 0L
}
