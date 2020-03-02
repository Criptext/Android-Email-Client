package com.criptext.mail.scenes.settings.aliases.data

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.AliasListItemListener
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

class AliasAdapter(private val mContext : Context,
                   private val aliasListItemListener: AliasListItemListener?,
                   private val aliasList: VirtualList<AliasItem>)
    : VirtualListAdapter(aliasList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is AliasHolder -> {
                val alias = aliasList[position]
                holder.bindAlias(alias)
                if(aliasListItemListener != null) {
                    holder.setOnClickListener {
                        aliasListItemListener.onAliasTrashClicked(alias, position)
                    }
                    holder.setOnSwitchListener {
                        aliasListItemListener.onAliasActiveSwitched(alias, position, it)
                    }
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView : View = View.inflate(mContext, R.layout.alias_item, null )
        return AliasHolder(itemView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {

    }

    override fun getActualItemId(position: Int): Long {
        return aliasList[position].id
    }

}
