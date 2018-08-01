package com.criptext.mail.scenes.search

import com.criptext.mail.scenes.mailbox.data.EmailThread
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by danieltigse on 6/14/18.
 */
class VirtualSearchThreadList(private val model: SearchSceneModel)
        : VirtualList<EmailThread> {
   override fun get(i: Int) = model.threads[i]

   override val size: Int
        get() = model.threads.size

    override val hasReachedEnd: Boolean
        get() = model.hasReachedEnd

}
