package com.criptext.mail.utils.virtuallist

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class VirtualRecyclerView(private val recyclerView: RecyclerView)
    : VirtualListView {

    private var virtualAdapter: VirtualListAdapter? = null
    private var mLayoutManager: LinearLayoutManager

    init {
        val ctx: Context = recyclerView.context
        mLayoutManager = LinearLayoutManager(ctx)
        recyclerView.layoutManager = mLayoutManager
    }

    override fun notifyDataSetChanged() {
        virtualAdapter?.notifyDataSetChanged()
    }

    override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
        virtualAdapter?.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun notifyItemRemoved(position: Int) {
        virtualAdapter?.notifyItemRemoved(position)
    }

    override fun notifyItemChanged(position: Int) {
        virtualAdapter?.notifyItemChanged(position)
    }

    override fun isOnTop(): Boolean {
        return mLayoutManager.findFirstCompletelyVisibleItemPosition()==0
    }

    override fun setAdapter(virtualListAdapter: VirtualListAdapter) {
        if (virtualAdapter == null) {
            virtualAdapter = virtualListAdapter
            recyclerView.adapter = virtualListAdapter
        }
    }

}