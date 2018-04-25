package com.email.utils.virtuallist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class VirtualRecyclerView(private val recyclerView: RecyclerView)
    : VirtualListView {

    private var virtualAdapter: VirtualListAdapter? = null

    init {
        val ctx: Context = recyclerView.context
        val mLayoutManager = LinearLayoutManager(ctx)
        recyclerView.layoutManager = mLayoutManager
    }

    override fun notifyThreadSetChanged() {
        virtualAdapter?.notifyDataSetChanged()
    }

    override fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int) {
        virtualAdapter?.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun notifyThreadRemoved(position: Int) {
        virtualAdapter?.notifyItemRemoved(position)
    }

    override fun notifyThreadChanged(position: Int) {
        virtualAdapter?.notifyItemChanged(position)
    }

    override fun setAdapter(virtualListAdapter: VirtualListAdapter) {
        if (virtualAdapter == null) {
            virtualAdapter = virtualListAdapter
            recyclerView.adapter = virtualListAdapter
        }
    }

}