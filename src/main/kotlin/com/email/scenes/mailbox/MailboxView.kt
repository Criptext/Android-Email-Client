package com.email.scenes.mailbox

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import com.email.IHostActivity
import com.email.R
import com.email.androidui.mailthread.ThreadListView
import com.email.androidui.mailthread.ThreadRecyclerView
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 1/23/18.
 */

interface MailboxScene : ThreadListView{

    fun setEmailList(threads: List<EmailThread>)
    fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener)

    class MailboxSceneView(private val sceneContainer: ViewGroup, private val mailboxView: View,
                           val hostActivity: IHostActivity) : MailboxScene {

        private val ctx = mailboxView.context

        private val recyclerView: RecyclerView by lazy {
            mailboxView.findViewById(R.id.mailbox_recycler) as RecyclerView
        }
        private val toolbar: Toolbar by lazy {
            mailboxView.findViewById<Toolbar>(R.id.mailbox_toolbar)
        }

        private lateinit var threadRecyclerView: ThreadRecyclerView

        var threadListener: EmailThreadAdapter.OnThreadEventListener? = null
            set(value) {
                threadRecyclerView.setThreadListener(value)
                field = value
            }
        override fun attachView(threadEventListener: EmailThreadAdapter.OnThreadEventListener) {
            threadRecyclerView = ThreadRecyclerView(recyclerView, threadEventListener)
            this.threadListener = threadEventListener

            sceneContainer.removeAllViews()
            sceneContainer.addView(mailboxView)
        }


        override fun setEmailList(threads: List<EmailThread>) {
            threadRecyclerView.setThreadList(threads)
        }

        override fun notifyThreadSetChanged() {
            threadRecyclerView.notifyThreadSetChanged()
        }

        override fun notifyThreadRemoved(position: Int) {
            threadRecyclerView.notifyThreadRemoved(position)
        }

        override fun notifyThreadChanged(position: Int) {
            threadRecyclerView.notifyThreadChanged(position)
        }

        override fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
            threadRecyclerView.notifyThreadRangeInserted(positionStart, itemCount)
        }
    }
}
