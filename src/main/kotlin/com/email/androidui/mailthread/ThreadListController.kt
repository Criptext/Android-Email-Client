package com.email.androidui.mailthread

import android.provider.ContactsContract
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private var threads : ArrayList<EmailThread>,
                           private val listView: ThreadListView?) {

    fun setThreadList(emails : ArrayList<EmailThread>): ArrayList<EmailThread> {
        threads = (emails as ArrayList<EmailThread>)
        return emails
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
}
