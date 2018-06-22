package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.db.models.FullEmail
import com.email.utils.virtuallist.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailRecyclerView(
        val recyclerView: RecyclerView,
        fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener?,
        val fullEmailList: VirtualList<FullEmail>) {

    val ctx: Context = recyclerView.context
    private val fullEmailListAdapter = FullEmailListAdapter(
            mContext = ctx,
            fullEmails = fullEmailList,
            fullEmailListener = fullEmailEventListener)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = fullEmailListAdapter
    }

    fun notifyFullEmailListChanged() {
        fullEmailListAdapter.notifyDataSetChanged()
    }

    fun scrollToLast() {
        recyclerView.scrollToPosition(fullEmailList.size - 1)
    }

    fun notifyFullEmailRemoved(position: Int) {
        fullEmailListAdapter.notifyItemRemoved(position)
    }

    fun notifyFullEmailChanged(position: Int) {
        fullEmailListAdapter.notifyItemChanged(position)
    }
}
