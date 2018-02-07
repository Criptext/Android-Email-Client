package com.email.androidui.labelthread

/**
 * Created by sebas on 1/30/18.
 */

interface LabelThreadListView {

    fun notifyLabelThreadSetChanged()
    fun notifyLabelThreadRemoved(position: Int)
    fun notifyLabelThreadRangeInserted(positionStart: Int, itemCount: Int)
    fun notifyLabelThreadChanged(position: Int)
}
