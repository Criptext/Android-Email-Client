package com.email.androidui.mailthread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.VirtualList

class ThreadRecyclerView(val recyclerView: RecyclerView,
                         threadEventListener: EmailThreadAdapter.OnThreadEventListener?,
                         threadList: VirtualList<EmailThread>)  {

    val ctx: Context = recyclerView.context
    private val emailThreadAdapter = EmailThreadAdapter(ctx, threadEventListener, threadList)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = emailThreadAdapter
    }

    fun setThreadListener(threadEventListener: EmailThreadAdapter.OnThreadEventListener?) {
        emailThreadAdapter.threadListener = threadEventListener
    }

    fun notifyThreadSetChanged() {
        emailThreadAdapter.notifyDataSetChanged()
    }

    fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
        emailThreadAdapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    fun notifyThreadRemoved(position: Int) {
        emailThreadAdapter.notifyItemRemoved(position)
    }

    fun notifyThreadChanged(position: Int) {
        emailThreadAdapter.notifyItemChanged(position)
    }

    fun changeMode(multiSelectON: Boolean, silent: Boolean) {
        if (emailThreadAdapter.isMultiSelectMode != multiSelectON) {
            emailThreadAdapter.isMultiSelectMode = multiSelectON
            if (!silent)
                notifyThreadSetChanged()
        }
    }
}