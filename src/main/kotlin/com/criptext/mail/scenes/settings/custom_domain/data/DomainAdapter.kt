package com.criptext.mail.scenes.settings.custom_domain.data

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.DomainListItemListener
import com.criptext.mail.utils.virtuallist.VirtualList
import com.criptext.mail.utils.virtuallist.VirtualListAdapter

class DomainAdapter(private val mContext : Context,
                    private val domainListItemListener: DomainListItemListener?,
                    private val domainList: VirtualList<DomainItem>)
    : VirtualListAdapter(domainList) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is DomainHolder -> {
                val device = domainList[position]
                holder.bindDevice(device)
                if(domainListItemListener != null) {
                    holder.setOnClickListener {
                        domainListItemListener.onCustomDomainTrashClicked(device, position)
                    }
                }
            }
        }
    }

    override fun onCreateActualViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView : View = View.inflate(mContext, R.layout.custom_domain_item, null )
        return DomainHolder(itemView)
    }

    override fun getActualItemViewType(position: Int): Int {
        return 1
    }

    override fun onApproachingEnd() {

    }

    override fun getActualItemId(position: Int): Long {
        return domainList[position].id.toLong()
    }

}
