package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.virtuallist.VirtualListView

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

    fun removeThreadsById(threadIds: List<String>) {
        for (threadId in threadIds) {
            val threadPosition = model.threads.indexOfFirst { thread -> thread.threadId == threadId }
            if (threadPosition > -1) {
                model.threads.removeAt(threadPosition)
            }
        }
        virtualListView?.notifyDataSetChanged()
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


    fun changeThreadReadStatus(threadIds: List<String>, unread: Boolean) {
        model.threads.forEachIndexed { index, emailPreview ->
            for (threadId in threadIds)
                if(emailPreview.threadId == threadId) {
                    model.threads[index] = emailPreview.copy(unread = unread)
                }
        }
        virtualListView?.notifyDataSetChanged()
    }

    fun updateThreadLabels(threadIds: List<String>, isStarred: Boolean) {
        model.threads.forEachIndexed { index, emailPreview ->
            for (threadId in threadIds)
                if(emailPreview.threadId == threadId) {
                    model.threads[index] = emailPreview.copy(isStarred = isStarred)
                }
        }
        virtualListView?.notifyDataSetChanged()
    }

    fun updateThreadsAndAddNew(emails: List<EmailPreview>){
        val newEmails = model.threads.filter { (it !in emails) && (it.timestamp.after(model.threads.first().timestamp)) }
        val oldEmails = model.threads.filter { it in emails }
        oldEmails.forEach {
            val index = model.threads.indexOf(it)
            if(index > -1){
                if(model.threads[index] != it)
                    model.threads[index] = it
            }
        }
        newEmails.forEach { model.threads.add(0, it) }
    }
}
