package com.email.scenes.mailbox

import com.email.db.DeliveryTypes
import com.email.email_preview.EmailPreview
import com.email.utils.virtuallist.VirtualListView

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private val model : MailboxSceneModel,
                           private val virtualListView: VirtualListView?) {

    private fun reset(emails : List<EmailPreview>) {
        model.threads.clear()
        model.threads.addAll(emails)
    }

    fun addNew(newThread: EmailPreview) {
        model.threads.add(0, newThread)
        virtualListView?.notifyDataSetChanged()
    }

    fun appendAll(loadedThreads : List<EmailPreview>, hasReachedEnd: Boolean) {
        model.threads.addAll(loadedThreads)

        model.hasReachedEnd = hasReachedEnd
        virtualListView?.notifyDataSetChanged()
    }

    fun select(thread: EmailPreview, position: Int) {
        model.selectedThreads.add(thread)
        virtualListView?.notifyItemChanged(position)
    }

    fun unselect(thread: EmailPreview, position: Int) {
        model.selectedThreads.remove(thread)
        virtualListView?.notifyItemChanged(position)
    }

    fun updateUnreadStatusAndNotifyItem(threadId: String, unread: Boolean){
        val threadPosition = model.threads.indexOfFirst { thread ->
            thread.threadId == threadId
        }

        if (threadPosition > -1){
            model.threads[threadPosition] = model.threads[threadPosition].copy(unread = unread)
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

    fun populateThreads(mailboxThreads: List<EmailPreview>) {
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

    fun replaceThread(thread: EmailPreview) {
        val position = model.threads.indexOfFirst { it.threadId == thread.threadId }
        if (position > -1) {
            model.threads[position] = thread
            virtualListView?.notifyItemChanged(position)
        }
    }

    fun markThreadAsOpened(emailId: Long) {
        val position = model.threads.indexOfFirst { it.emailId == emailId }
        if (position > -1) {
            model.threads[position] =
                    model.threads[position].copy(deliveryStatus = DeliveryTypes.READ)
            virtualListView?.notifyItemChanged(position)
        }
    }
}
