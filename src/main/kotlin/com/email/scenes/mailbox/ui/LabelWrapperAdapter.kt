package com.email.scenes.mailbox.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.scenes.mailbox.DrawerMenuItemListener
import com.email.scenes.settings.SettingsUIObserver
import com.email.utils.virtuallist.VirtualList
import com.email.utils.virtuallist.VirtualListAdapter

/**
 * Created by danieltigse on 28/06/18.
 */

class LabelWrapperAdapter(private val mContext : Context,
                          private var drawerMenuItemListener: DrawerMenuItemListener?,
                          private val labelList: VirtualList<LabelWrapper>)
    : VirtualListAdapter(labelList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(holder){
            is LabelHolder -> {
                if(holder.itemView == null) return
                val labelThread = labelList[position]
                holder.bindLabel(labelThread)
                holder.setOnClickedListener {
                    drawerMenuItemListener?.onCustomLabelClicked(labelThread.label)
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView : View = View.inflate(mContext, R.layout.label_item, null )
        return LabelHolder(itemView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {

    }

    override fun getActualItemId(position: Int): Long {
        return labelList[position].id
    }

}
