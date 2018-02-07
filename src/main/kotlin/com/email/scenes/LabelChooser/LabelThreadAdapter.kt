package com.email.scenes.LabelChooser

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.email.R
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.LabelChooser.holders.LabelHolder

/**
 * Created by sebas on 2/2/18.
 */

class LabelThreadAdapter(val mContext : Context,
                         var labelThreadListener : OnLabelThreadEventListener?,
                         val labelThreadListHandler: DialogLabelsChooser.LabelThreadListHandler)
    : RecyclerView.Adapter<LabelHolder>() {

    fun onToggleLabelSelection(labelThread: LabelThread, position: Int) {
        labelThreadListener?.onToggleLabelSelection(labelThread, position)
    }
    override fun onBindViewHolder(holder: LabelHolder?, position: Int) {
        if(holder?.itemView == null) return
        val labelThread = labelThreadListHandler.getLabelThreadFromIndex(position)
        holder.bindLabel(labelThread)

        holder.checkBoxView.setOnClickListener {
            onToggleLabelSelection(labelThread, position)
            true
        }
        holder.itemView.setOnClickListener(
                {
                    onToggleLabelSelection(labelThread, position)
                    true
                })
    }

    override fun getItemCount(): Int {
        return labelThreadListHandler.getLabelThreadsCount()
    }


    private fun createMailItemView(): View {
        val labelItemView = View.inflate(mContext, R.layout.label_item, null)
        return labelItemView
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelHolder {
        val itemView : View = createMailItemView()
        return LabelHolder(itemView)
    }

    interface OnLabelThreadEventListener{
        fun onToggleLabelSelection(label: LabelThread, position: Int)
    }
}
