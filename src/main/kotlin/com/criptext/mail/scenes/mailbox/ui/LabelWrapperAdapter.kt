package com.criptext.mail.scenes.mailbox.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.DrawerMenuItemListener
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

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
