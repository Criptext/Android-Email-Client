package com.email.androidui.labelthread

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.email.scenes.LabelChooser.LabelChooserDialog
import com.email.scenes.LabelChooser.LabelThreadAdapter
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.utils.VirtualList

class LabelThreadRecyclerView(recyclerView: RecyclerView,
                              threadEventListener: LabelThreadAdapter.OnLabelThreadEventListener?,
                              labelsList: VirtualList<LabelThread>)  {

    private val ctx: Context = recyclerView.context
    private val labelThreadAdapter = LabelThreadAdapter(ctx, threadEventListener, labelsList)

    init {
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        Log.d("LabelThreadRecyclerView", "adapter ${labelsList.size}")
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