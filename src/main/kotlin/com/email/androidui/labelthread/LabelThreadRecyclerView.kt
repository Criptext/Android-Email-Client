package com.email.androidui.labelthread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.labelChooser.LabelThreadAdapter
import com.email.scenes.labelChooser.data.LabelThread
import com.email.utils.VirtualList

class LabelThreadRecyclerView(recyclerView: RecyclerView,
                              threadEventListener: LabelThreadAdapter.OnLabelThreadEventListener?,
                              labelsList: VirtualList<LabelThread>)  {

    private val ctx: Context = recyclerView.context
    private val labelThreadAdapter = LabelThreadAdapter(ctx, threadEventListener, labelsList)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        recyclerView.adapter = labelThreadAdapter
    }

    fun setThreadListener(labelThreadEventListener: LabelThreadAdapter.OnLabelThreadEventListener?) {
        labelThreadAdapter.labelThreadListener = labelThreadEventListener
    }

    fun notifyLabelThreadSetChanged() {
        labelThreadAdapter.notifyDataSetChanged()
    }

    fun notifyLabelThreadRangeInserted(positionStart: Int, itemCount: Int) {
        labelThreadAdapter.notifyItemRangeInserted(positionStart, itemCount)
    }

    fun notifyLabelThreadChanged(position: Int) {
         labelThreadAdapter.notifyItemChanged(position)
    }

    fun notifyLabelThreadRemoved(position: Int) {
        labelThreadAdapter.notifyItemRemoved(position)
    }
}