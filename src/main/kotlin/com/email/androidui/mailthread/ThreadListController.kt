package com.email.androidui.mailthread

import android.provider.ContactsContract
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private var threads : ArrayList<EmailThread>,
                           private val listView: ThreadListView?) {

    fun setThreadList(emails : List<EmailThread>) {
        threads.clear()
        threads.addAll(emails)
    }

    fun appendThreads(emails : List<EmailThread>) {
        threads.addAll(emails)
    }

    fun removeByThread(id: String) {
        val threadPosition = removeThreadById(threads, id)
        listView?.notifyThreadRemoved(threadPosition)
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
        listView?.notifyThreadSetChanged()
    }
}
