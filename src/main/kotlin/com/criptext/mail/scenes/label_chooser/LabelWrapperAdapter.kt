package com.criptext.mail.scenes.label_chooser

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.label_chooser.holders.LabelHolder
import com.criptext.mail.utils.virtuallist.VirtualList

/**
 * Created by sebas on 2/2/18.
 */

class LabelWrapperAdapter(private val mContext : Context,
                          var labelWrapperListener : OnLabelWrapperEventListener?,
                          private val labelList: VirtualList<LabelWrapper>)
    : RecyclerView.Adapter<LabelHolder>() {

    private fun onToggleLabelSelection(labelWrapper: LabelWrapper, position: Int) {
        labelWrapperListener?.onToggleLabelSelection(labelWrapper, position)
    }

    override fun onBindViewHolder(holder: LabelHolder, position: Int) {
        if(holder.itemView == null) return
        val labelThread = labelList[position]
        holder.bindLabel(labelThread)

        val itemClickListener = {
            onToggleLabelSelection(labelThread, position)
        }
        holder.setOnCheckboxClickedListener(itemClickListener)

        holder.itemView.setOnClickListener({
            onToggleLabelSelection(labelThread, position)
        })
    }

    override fun getItemCount(): Int {
        return labelList.size
    }

    private fun createMailItemView(): View {
        return View.inflate(mContext, R.layout.label_item, null )
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelHolder {
        val itemView : View = createMailItemView()
        return LabelHolder(itemView)
    }

    interface OnLabelWrapperEventListener{
        fun onToggleLabelSelection(label: LabelWrapper, position: Int)
    }
}
