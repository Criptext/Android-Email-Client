package com.email.androidui.labelwrapper

/**
 * Created by sebas on 1/30/18.
 */

interface LabelWrapperListView {

    fun notifyLabelWrapperSetChanged()
    fun notifyLabelWrapperRemoved(position: Int)
    fun notifyLabelWrapperRangeInserted(positionStart: Int, itemCount: Int)
    fun notifyLabelWrapperChanged(position: Int)
}
