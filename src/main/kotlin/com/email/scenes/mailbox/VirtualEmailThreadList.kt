package com.email.scenes.mailbox

import com.email.email_preview.EmailPreview
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.virtuallist.VirtualList

/**
 * Created by gabriel on 4/25/18.
 */
class VirtualEmailThreadList(private val model: MailboxSceneModel)
        : VirtualList<EmailPreview> {
   override fun get(i: Int) = model.threads[i]

   override val size: Int
        get() = model.threads.size

    override val hasReachedEnd: Boolean
        get() = model.hasReachedEnd

    val isInMultiSelectMode: Boolean
        get() = model.isInMultiSelect


}
