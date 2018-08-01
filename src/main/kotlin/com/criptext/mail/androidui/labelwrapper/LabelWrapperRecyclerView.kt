package com.criptext.mail.androidui.labelwrapper

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.criptext.mail.scenes.label_chooser.LabelWrapperAdapter
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.virtuallist.VirtualList

class LabelWrapperRecyclerView(recyclerView: RecyclerView,
                               threadEventListener: LabelWrapperAdapter.OnLabelWrapperEventListener?,
                               labelsList: VirtualList<LabelWrapper>)  {

    private val ctx: Context = recyclerView.context
    private val labelWrapperAdapter = LabelWrapperAdapter(ctx, threadEventListener, labelsList)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = labelWrapperAdapter
    }

    fun setLabelWrapperListener(labelWrapperEventListener: LabelWrapperAdapter.OnLabelWrapperEventListener?) {
        labelWrapperAdapter.labelWrapperListener = labelWrapperEventListener
    }

    fun notifyLabelWrapperSetChanged() {
        labelWrapperAdapter.notifyDataSetChanged()
    }

    fun notifyLabelWrapperRangeInserted(positionStart: Int, itemCount: Int) {
        labelWrapperAdapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    fun notifyLabelWrapperChanged(position: Int) {
         labelWrapperAdapter.notifyItemChanged(position)
    }

    fun notifyLabelWrapperRemoved(position: Int) {
        labelWrapperAdapter.notifyItemRemoved(position)
    }
}