package com.criptext.mail.utils.virtuallist

/**
 * Created by sebas on 1/30/18.
 */

interface VirtualListView {

    fun notifyDataSetChanged()

    fun notifyItemRemoved(position: Int)

    fun notifyItemChanged(position: Int)

    fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)

    fun setAdapter(virtualListAdapter: VirtualListAdapter)
}
