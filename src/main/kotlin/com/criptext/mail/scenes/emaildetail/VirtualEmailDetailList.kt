package com.criptext.mail.scenes.emaildetail

import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by gabriel on 4/25/18.
 */
class VirtualEmailDetailList(private val model: EmailDetailSceneModel)
        : VirtualList<FullEmail> {
   override fun get(i: Int) = model.emails[i]

   override val size: Int
        get() = model.emails.size

    override val hasReachedEnd: Boolean
        get() = true
}
