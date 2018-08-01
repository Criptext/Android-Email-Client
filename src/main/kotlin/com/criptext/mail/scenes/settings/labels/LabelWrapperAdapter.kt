package com.criptext.mail.scenes.settings.labels

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 28/06/18.
 */

class LabelWrapperAdapter(private val mContext : Context,
                          private var settingsUIObserver: SettingsUIObserver?,
                          private val labelList: VirtualList<LabelWrapper>)
    : VirtualListAdapter(labelList) {

    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder){
            is LabelHolder -> {
                if(holder.itemView == null) return
                val labelThread = labelList[position]
                holder.bindLabel(labelThread)
                holder.setOnCheckboxClickedListener({
                    labelThread.isSelected = !labelThread.isSelected
                    settingsUIObserver?.onToggleLabelSelection(labelThread)
                })
            }
            is FooterLabelHolder -> {
                holder.setOnCreateLabelClickedListener {
                    settingsUIObserver?.onCreateLabelClicked()
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType){
            TYPE_ITEM -> {
                val itemView : View = View.inflate(mContext, R.layout.label_item, null )
                LabelHolder(itemView)
            }
            else -> {
                val itemView : View = View.inflate(mContext, R.layout.label_footer_holder, null )
                FooterLabelHolder(itemView)
            }
        }

    }

    override fun getActualItemViewType(position: Int): Int {
        return TYPE_ITEM
    }

    override fun getItemViewType(position: Int): Int {
        if (position == labelList.size) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    override fun getItemCount(): Int {
        return labelList.size + 1
    }

    override fun onApproachingEnd() {

    }

    override fun getActualItemId(position: Int): Long {
        return labelList[position].id
    }

}
