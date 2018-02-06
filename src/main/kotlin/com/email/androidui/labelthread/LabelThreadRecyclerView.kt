package com.email.androidui.labelthread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.email.scenes.LabelChooser.DialogLabelsChooser
import com.email.scenes.LabelChooser.LabelThreadAdapter

class LabelThreadRecyclerView(val recyclerView: RecyclerView,
                              threadEventListener: LabelThreadAdapter.OnLabelThreadEventListener?,
                              labelThreadListHandler: DialogLabelsChooser.LabelThreadListHandler)  {

    val ctx: Context = recyclerView.context
    private val labelThreadAdapter = LabelThreadAdapter(ctx, threadEventListener, labelThreadListHandler)

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
         labelThreadAdapter.notifyDataSetChanged()
    }

    fun notifyLabelThreadRemoved(position: Int) {
        labelThreadAdapter.notifyItemRemoved(position)
    }
}