package com.email.scenes.mailbox

import com.email.utils.virtuallist.VirtualListView
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private val model : MailboxSceneModel,
                           private val virtualListView: VirtualListView?) {

    fun setThreadList(emails : List<EmailThread>) {
        model.threads.clear()
        model.threads.addAll(emails)
    }

    fun appendThreads(loadedThreads : List<EmailThread>, hasReachedEnd: Boolean) {
        model.threads.addAll(loadedThreads)

        model.hasReachedEnd = hasReachedEnd
        virtualListView?.notifyThreadSetChanged()
    }

    fun removeByThread(id: String) {
        val threadPosition = removeThreadById(model.threads, id)
        virtualListView?.notifyThreadRemoved(threadPosition)
    }


    companion object {
        fun removeThreadById(threads: ArrayList<EmailThread>, threadId: String): Int {
            val threadPosition = threads.indexOfFirst { thread -> thread.threadId == threadId }
            if (threadPosition > -1)
                threads.removeAt(threadPosition)
            return threadPosition
        }
    }

    fun populateThreads(mailboxThreads: List<EmailThread>) {
        setThreadList(mailboxThreads)
        virtualListView?.notifyThreadSetChanged()
    }

    fun toggleMultiSelectMode(multiSelectON: Boolean, silent: Boolean) {
        if (model.isInMultiSelect != multiSelectON) {
            model.isInMultiSelect = multiSelectON
            if (!silent)
                virtualListView?.notifyThreadSetChanged()
        }
    }
}
