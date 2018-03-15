package com.email.scenes.emaildetail.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.db.models.FullEmail
import com.email.utils.VirtualList

/**
 * Created by sebas on 3/12/18.
 */

class FullEmailRecyclerView(
        val recyclerView: RecyclerView,
        fullEmailEventListener: FullEmailListAdapter.OnFullEmailEventListener?,
        fullEmailList: VirtualList<FullEmail>) {

    val ctx: Context = recyclerView.context
    private val fullEmailListAdapter = FullEmailListAdapter(
            mContext = ctx,
            fullEmails = fullEmailList,
            fullEmailListener = fullEmailEventListener)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = fullEmailListAdapter
    }

    fun notifyThreadSetChanged() {
        fullEmailListAdapter.notifyDataSetChanged()
    }

    fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
        fullEmailListAdapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    fun notifyThreadRemoved(position: Int) {
        fullEmailListAdapter.notifyItemRemoved(position)
    }

    fun notifyThreadChanged(position: Int) {
        fullEmailListAdapter.notifyItemChanged(position)
    }
}
