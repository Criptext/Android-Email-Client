package com.email.utils.virtuallist

import com.email.utils.virtuallist.VirtualListAdapter

/**
 * Created by sebas on 1/30/18.
 */

interface VirtualListView {

    fun notifyThreadSetChanged()

    fun notifyThreadRemoved(position: Int)

    fun notifyThreadChanged(position: Int)

    fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int)

    fun setAdapter(virtualListAdapter: VirtualListAdapter)
}
