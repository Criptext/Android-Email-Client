package com.email.androidui.mailthread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.email.scenes.mailbox.OnScrollListener
import com.email.scenes.mailbox.ui.EmailThreadAdapter
import com.email.scenes.mailbox.data.EmailThread
import com.email.utils.VirtualList

class ThreadRecyclerView(val recyclerView: RecyclerView,
                         threadEventListener: EmailThreadAdapter.OnThreadEventListener?,
                         onScrollListener: OnScrollListener,
                         threadList: VirtualList<EmailThread>)  {

    val ctx: Context = recyclerView.context
    private var emailThreadAdapter = EmailThreadAdapter(ctx, threadEventListener, threadList)

    init {
        val mLayoutManager = LinearLayoutManager(ctx)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = emailThreadAdapter

        recyclerView.addOnScrollListener( object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = mLayoutManager.childCount
                val totalItemCount = mLayoutManager.itemCount
                val pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition()
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    if(dy > 0) {
                        onScrollListener.onReachEnd()
                    } else return // Scrolling Up
                }
            }
        })
    }

    fun setEmailThreadAdapter(emailThreadAdapter: EmailThreadAdapter) {
        this.emailThreadAdapter = emailThreadAdapter
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