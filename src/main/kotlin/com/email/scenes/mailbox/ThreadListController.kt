package com.email.scenes.mailbox

import com.email.utils.virtuallist.VirtualListView
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private val model : MailboxSceneModel,
                           private val virtualListView: VirtualListView?) {

    private fun reset(emails : List<EmailThread>) {
        model.threads.clear()
        model.threads.addAll(emails)
    }

    fun addNew(newThread: EmailThread) {
        model.threads.add(0, newThread)
        virtualListView?.notifyDataSetChanged()
    }

    fun appendAll(loadedThreads : List<EmailThread>, hasReachedEnd: Boolean) {
        model.threads.addAll(loadedThreads)

        model.hasReachedEnd = hasReachedEnd
        virtualListView?.notifyDataSetChanged()
    }

    fun select(thread: EmailThread, position: Int) {
        model.selectedThreads.add(thread)
        virtualListView?.notifyItemChanged(position)
    }

    fun unselect(thread: EmailThread, position: Int) {
        model.selectedThreads.remove(thread)
        virtualListView?.notifyItemChanged(position)
    }

    fun updateUnreadStatusAndNotifyItem(threadId: String, unread: Boolean){
        val threadPosition = model.threads.indexOfFirst { thread ->
            thread.latestEmail.email.unread = unread
            thread.threadId == threadId
        }
        if (threadPosition > -1){
            virtualListView?.notifyItemChanged(threadPosition)
        }
    }

    fun removeThreadById(threadId: String) {
        val threadPosition = model.threads.indexOfFirst { thread -> thread.threadId == threadId }
        if (threadPosition > -1) {
            model.threads.removeAt(threadPosition)
            virtualListView?.notifyItemRemoved(threadPosition)
        }
    }

    fun populateThreads(mailboxThreads: List<EmailThread>) {
        reset(mailboxThreads)
        virtualListView?.notifyDataSetChanged()
    }

    fun toggleMultiSelectMode(multiSelectON: Boolean, silent: Boolean) {
        if (model.isInMultiSelect != multiSelectON) {
            model.isInMultiSelect = multiSelectON
            if (!silent)
                virtualListView?.notifyDataSetChanged()
        }
    }

    fun clear() {
        model.threads.clear()
        virtualListView?.notifyDataSetChanged()
    }

    fun reRenderAll() {
        virtualListView?.notifyDataSetChanged()
    }
}
