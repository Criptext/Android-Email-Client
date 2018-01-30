package com.email.androidui.mailthread

/**
 * Created by sebas on 1/30/18.
 */

interface ThreadListView {

    fun notifyThreadSetChanged()

    fun notifyThreadRemoved(position: Int)

    fun notifyThreadChanged(position: Int)

    fun notifyThreadRangeInserted(positionStart: Int, itemCount: Int)
}
