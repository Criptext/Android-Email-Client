package com.email.androidui.mailthread

import android.provider.ContactsContract
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/31/18.
 */

class ThreadListController(private val threads : ArrayList<EmailThread>,
                           private val listView: ThreadListView?) {

    constructor(threads : ArrayList<EmailThread>): this(threads, null)

    fun populateThreads(emails : List<EmailThread>): List<EmailThread> {
        threads.addAll(emails)
        return threads
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
