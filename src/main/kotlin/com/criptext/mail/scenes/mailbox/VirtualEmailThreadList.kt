package com.criptext.mail.scenes.mailbox

import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.virtuallist.VirtualList

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
